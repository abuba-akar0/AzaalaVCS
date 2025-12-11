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
        System.err.println("✗ Commit error: " + exception.getMessage());
        LOGGER.severe("Commit error: " + exception.getMessage());
    }
}

