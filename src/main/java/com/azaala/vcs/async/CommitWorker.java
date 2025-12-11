package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.persistence.DatabaseManager;
import com.azaala.vcs.persistence.dao.*;
import com.azaala.vcs.persistence.models.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * CommitWorker - Handles commit operations with database integration
 * Executes as a background task with transaction management
 */
public class CommitWorker extends BaseVCSWorker<String> {
    private static final Logger LOGGER = Logger.getLogger(CommitWorker.class.getName());
    private final VCS vcs;
    private final Repository repository;
    private final String commitMessage;
    private final DatabaseManager dbManager;
    private final CommitDAO commitDAO;
    private final StagedFileDAO stagedFileDAO;
    private final CommitFileDAO commitFileDAO;
    private final ActivityLogDAO activityLogDAO;
    private final RepositoryDAO repositoryDAO;

    public CommitWorker(VCS vcs, Repository repository, String commitMessage, ProgressListener progressListener) {
        super(progressListener);
        this.vcs = vcs;
        this.repository = repository;
        this.commitMessage = commitMessage;
        this.dbManager = DatabaseManager.getInstance();
        this.commitDAO = new CommitDAO();
        this.stagedFileDAO = new StagedFileDAO();
        this.commitFileDAO = new CommitFileDAO();
        this.activityLogDAO = new ActivityLogDAO();
        this.repositoryDAO = new RepositoryDAO();
    }

    public CommitWorker(VCS vcs, Repository repository, String commitMessage) {
        this(vcs, repository, commitMessage, null);
    }

    @Override
    protected String doInBackground() throws Exception {
        try {
            publishProgress("Validating repository...", 10);
            if (repository == null || repository.getRepositoryPath() == null) {
                throw new Exception("No active repository");
            }

            publishProgress("Retrieving repository information...", 15);
            Long repoId = repository.getRepoId();

            // If repository ID not set, try to fetch from database
            if (repoId == null) {
                try {
                    RepositoryEntity repoEntity = repositoryDAO.findByPath(repository.getPath());
                    if (repoEntity != null) {
                        repoId = repoEntity.getRepoId();
                        repository.setRepoId(repoId);
                        System.out.println("✓ Repository ID resolved from database: " + repoId);
                    }
                } catch (Exception e) {
                    System.out.println("Note: Could not resolve repository ID from database: " + e.getMessage());
                }
            }

            if (repoId == null) {
                throw new Exception("Repository ID not found. Please initialize or reload the repository.");
            }

            // Make final copy for use in lambda expressions
            final Long finalRepoId = repoId;

            publishProgress("Retrieving staged files...", 20);

            // Try to get staged files from database first
            List<String> filePathsToCommit = null;

            try {
                List<StagedFileEntity> stagedFiles = stagedFileDAO.findByRepoId(finalRepoId);
                if (stagedFiles != null && !stagedFiles.isEmpty()) {
                    System.out.println("✓ Found " + stagedFiles.size() + " staged files in database");
                    filePathsToCommit = new java.util.ArrayList<>();
                    for (StagedFileEntity file : stagedFiles) {
                        filePathsToCommit.add(file.getFilePath());
                    }
                }
            } catch (Exception e) {
                System.out.println("Note: Could not retrieve staged files from database: " + e.getMessage());
            }

            // Fallback: Get staged files from filesystem if database is empty
            if (filePathsToCommit == null || filePathsToCommit.isEmpty()) {
                System.out.println("Falling back to filesystem-based staged files list...");
                filePathsToCommit = repository.getStagedFiles();
                if (filePathsToCommit == null || filePathsToCommit.isEmpty()) {
                    throw new Exception("No files staged for commit");
                }
                System.out.println("✓ Found " + filePathsToCommit.size() + " staged files in filesystem");
            }

            publishProgress("Creating commit: " + commitMessage.substring(0, Math.min(30, commitMessage.length())) + "...", 40);

            // Make final copy of filePathsToCommit for use in lambda
            final List<String> finalFilePathsToCommit = filePathsToCommit;

            // Execute commit with transaction
            String commitId = dbManager.executeTransaction(conn -> {
                // Create commit in VCS
                String cId = vcs.commit(commitMessage);
                if (cId == null || cId.isEmpty()) {
                    throw new RuntimeException("Failed to create commit in VCS");
                }

                publishProgress("Saving commit to database...", 50);

                // Create commit entity
                CommitEntity commitEntity = new CommitEntity(
                    cId,
                    finalRepoId,
                    commitMessage,
                    commitMessage.substring(0, Math.min(100, commitMessage.length())),
                    System.getProperty("user.name", "Unknown"),
                    LocalDateTime.now(),
                    finalFilePathsToCommit.size(),
                    LocalDateTime.now()
                );

                // Save commit using DAO
                boolean commitSaved = commitDAO.create(commitEntity);
                if (!commitSaved) {
                    throw new RuntimeException("Failed to save commit to database");
                }

                publishProgress("Recording committed files...", 60);

                // Add files to commit_files table
                int filesProcessed = 0;
                for (String filePath : finalFilePathsToCommit) {
                    CommitFileEntity commitFile = new CommitFileEntity(
                        cId,
                        finalRepoId,
                        filePath
                    );

                    // Try to get file size if available
                    try {
                        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
                        long fileSize = java.nio.file.Files.size(path);
                        commitFile.setFileSize(fileSize);
                    } catch (Exception e) {
                        commitFile.setFileSize(0);
                    }

                    commitFile.setStatus("added");
                    commitFile.setCreatedAt(LocalDateTime.now());
                    Long fileId = commitFileDAO.addFile(commitFile);
                    if (fileId == null) {
                        throw new RuntimeException("Failed to record committed file: " + filePath);
                    }
                    filesProcessed++;
                }

                publishProgress("Clearing staging area...", 75);

                // Clear staged files
                int cleared = stagedFileDAO.deleteByRepoId(finalRepoId);
                System.out.println("  Cleared " + cleared + " staged files from database");

                publishProgress("Updating repository metadata...", 85);

                // Update repository last commit time
                RepositoryEntity repo = repositoryDAO.findById(finalRepoId);
                if (repo != null) {
                    repo.setLastCommitAt(LocalDateTime.now());
                    repositoryDAO.update(repo);
                }

                publishProgress("Logging activity...", 90);

                // Log activity
                ActivityLogEntity activityLog = new ActivityLogEntity(
                    finalRepoId,
                    "COMMIT",
                    "Committed " + filesProcessed + " files. Message: " + commitMessage
                );
                activityLogDAO.create(activityLog);

                System.out.println("✓ Transaction committed successfully");
                return cId;
            });

            publishProgress("Commit completed: " + commitId, 100);
            return commitId;

        } catch (Exception e) {
            System.err.println("✗ Commit operation failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void onSuccess(String result) {
        System.out.println("✓ Commit completed successfully. ID: " + result);
    }

    @Override
    protected void onError(Exception exception) {
        String errorMessage = exception.getMessage();

        // Log to file for debugging (not shown to user)
        LOGGER.severe("Commit error: " + errorMessage);

        // Show ONLY user-friendly error dialog (no technical console output)
        showErrorDialog(errorMessage);
    }

    /**
     * Display user-friendly error dialog based on error type
     */
    private void showErrorDialog(String errorMessage) {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                String title = "Commit Failed";
                String userMessage = buildUserFriendlyMessage(errorMessage);

                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    userMessage,
                    title,
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );
            });
        } catch (Exception e) {
            System.err.println("Could not display error dialog: " + e.getMessage());
        }
    }

    /**
     * Build user-friendly error message based on error type
     */
    private String buildUserFriendlyMessage(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "Unknown error occurred";
        }

        String userMessage = "";

        // Handle specific error types
        if (errorMessage.contains("No files staged")) {
            userMessage = "❌ No Files Staged for Commit\n\n" +
                "You need to stage files before committing.\n\n" +
                "How to fix this:\n" +
                "1. Click the 'Add File' button to add individual files\n" +
                "2. Or click 'Add All Files' to stage all files in your repository\n" +
                "3. Then click 'Commit' again\n\n" +
                "📍 Tip: Use the Status tab to see which files are staged";

        } else if (errorMessage.contains("Repository ID not found")) {
            userMessage = "❌ Repository Not Properly Initialized\n\n" +
                "The repository is missing required information.\n\n" +
                "How to fix this:\n" +
                "1. Go to File → New Repository (or Open Repository)\n" +
                "2. Initialize or reload the repository\n" +
                "3. Try committing again\n\n" +
                "📍 Tip: Make sure your repository is properly initialized";

        } else if (errorMessage.contains("commit message") || errorMessage.contains("Message")) {
            userMessage = "❌ Invalid Commit Message\n\n" +
                "Your commit message is invalid.\n\n" +
                "How to fix this:\n" +
                "1. Enter a meaningful commit message (at least 1 character)\n" +
                "2. Describe what changes you made\n" +
                "3. Try committing again\n\n" +
                "📍 Example: 'Fixed login bug' or 'Updated user interface'";

        } else if (errorMessage.contains("Failed to copy file")) {
            userMessage = "❌ File Copy Error\n\n" +
                "Could not copy files during commit operation.\n\n" +
                "Possible causes:\n" +
                "• File permissions denied\n" +
                "• File is locked or in use\n" +
                "• Disk space is full\n" +
                "• File path is too long\n\n" +
                "How to fix this:\n" +
                "1. Close any applications using these files\n" +
                "2. Check file permissions\n" +
                "3. Ensure sufficient disk space\n" +
                "4. Try committing again";

        } else if (errorMessage.contains("database") || errorMessage.contains("Database")) {
            userMessage = "❌ Database Error\n\n" +
                "Could not save commit to database.\n\n" +
                "Possible causes:\n" +
                "• Database connection lost\n" +
                "• Database is locked\n" +
                "• Database file corrupted\n\n" +
                "How to fix this:\n" +
                "1. Restart the application\n" +
                "2. Check your database connection\n" +
                "3. If problem persists, contact support\n\n" +
                "📍 Error: " + errorMessage;

        } else if (errorMessage.contains("Failed to create commit")) {
            userMessage = "❌ Commit Creation Failed\n\n" +
                "The commit could not be created in the version control system.\n\n" +
                "Possible causes:\n" +
                "• Invalid file paths\n" +
                "• Files were deleted after staging\n" +
                "• System disk full\n" +
                "• Permission denied\n\n" +
                "How to fix this:\n" +
                "1. Verify all staged files still exist\n" +
                "2. Check file permissions\n" +
                "3. Ensure sufficient disk space\n" +
                "4. Try again with fewer files\n\n" +
                "📍 Technical: " + errorMessage;

        } else {
            // Generic error message with technical details
            userMessage = "❌ Commit Operation Failed\n\n" +
                "An error occurred while creating the commit.\n\n" +
                "Error Details:\n" +
                errorMessage + "\n\n" +
                "What to try:\n" +
                "1. Check the Status tab for file information\n" +
                "2. Make sure at least one file is staged\n" +
                "3. Enter a valid commit message\n" +
                "4. Try again\n\n" +
                "If the problem persists, check the application logs for more details";
        }

        return userMessage;
    }
}

