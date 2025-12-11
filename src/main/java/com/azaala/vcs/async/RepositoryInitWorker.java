package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.persistence.DatabaseManager;
import com.azaala.vcs.persistence.DatabaseException;
import com.azaala.vcs.persistence.dao.RepositoryDAO;
import com.azaala.vcs.persistence.dao.ActivityLogDAO;
import com.azaala.vcs.persistence.models.RepositoryEntity;
import com.azaala.vcs.persistence.models.ActivityLogEntity;

import java.io.File;
import java.time.LocalDateTime;

/**
 * RepositoryInitWorker - Initializes a new repository with database persistence
 */
public class RepositoryInitWorker extends BaseVCSWorker<Boolean> {
    private final VCS vcs;
    private final String repositoryPath;
    private final String repositoryName;
    private final String description;
    private final RepositoryDAO repositoryDAO;
    private final ActivityLogDAO activityLogDAO;

    public RepositoryInitWorker(VCS vcs, String repositoryPath, String repositoryName, String description, ProgressListener progressListener) {
        super(progressListener);
        this.vcs = vcs;
        this.repositoryPath = repositoryPath;
        this.repositoryName = repositoryName;
        this.description = description;
        this.repositoryDAO = new RepositoryDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public RepositoryInitWorker(VCS vcs, String repositoryPath, String repositoryName, String description) {
        this(vcs, repositoryPath, repositoryName, description, null);
    }

    public RepositoryInitWorker(VCS vcs, String repositoryPath, ProgressListener progressListener) {
        this(vcs, repositoryPath, new File(repositoryPath).getName(), "Repository initialized", progressListener);
    }

    public RepositoryInitWorker(VCS vcs, String repositoryPath) {
        this(vcs, repositoryPath, null);
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            publishProgress("Validating repository path...", 10);
            File repoDir = new File(repositoryPath);
            if (!repoDir.exists()) {
                throw new Exception("Repository path does not exist: " + repositoryPath);
            }

            publishProgress("Initializing repository in VCS...", 20);
            boolean vcsInitResult = vcs.initRepository(repositoryPath);
            if (!vcsInitResult) {
                throw new Exception("Failed to initialize repository in VCS");
            }

            publishProgress("Saving repository metadata to database...", 50);

            // Create repository entity
            String repoName = repositoryName != null ? repositoryName : new File(repositoryPath).getName();
            RepositoryEntity repository = new RepositoryEntity(
                repoName,
                repositoryPath
            );
            repository.setDescription(description != null ? description : "Repository at " + repositoryPath);
            repository.setCreatedAt(LocalDateTime.now());

            // Save to database
            Long repoId = repositoryDAO.create(repository);
            if (repoId == null) {
                throw new Exception("Failed to save repository to database");
            }

            publishProgress("Logging initialization activity...", 75);

            // Log activity
            ActivityLogEntity activityLog = new ActivityLogEntity(
                repoId,
                "INIT",
                "Repository initialized. Path: " + repositoryPath
            );
            activityLogDAO.create(activityLog);

            publishProgress("Repository initialization completed successfully", 100);
            System.out.println("✓ Repository initialized with ID: " + repoId);
            return true;

        } catch (Exception e) {
            System.err.println("✗ Repository initialization failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void onSuccess(Boolean result) {
        if (result) {
            System.out.println("✓ Repository initialization completed successfully");
        } else {
            System.out.println("✗ Repository initialization failed");
        }
    }

    @Override
    protected void onError(Exception exception) {
        String errorMessage = exception.getMessage();

        // Show ONLY user-friendly error dialog (no technical console output)
        showErrorDialog(errorMessage);
    }

    /**
     * Display user-friendly error dialog for repository initialization
     */
    private void showErrorDialog(String errorMessage) {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                String title = "Repository Initialization Failed";
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
     * Build user-friendly message for repository initialization errors
     */
    private String buildUserFriendlyMessage(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "Unknown error occurred";
        }

        String userMessage = "";

        if (errorMessage.contains("Permission denied") || errorMessage.contains("Permission")) {
            userMessage = "❌ Permission Denied\n\n" +
                "You don't have permission to create a repository here.\n\n" +
                "How to fix this:\n" +
                "1. Choose a different directory\n" +
                "2. Or run with administrator privileges\n" +
                "3. Check folder permissions\n\n" +
                "📍 Try selecting a folder in Documents or Desktop";

        } else if (errorMessage.contains("already exists")) {
            userMessage = "❌ Repository Already Exists\n\n" +
                "This directory is already a VCS repository.\n\n" +
                "Options:\n" +
                "1. Choose a different empty directory\n" +
                "2. Or open this repository\n" +
                "3. Delete the existing repository if you want to start fresh";

        } else if (errorMessage.contains("Path does not exist") || errorMessage.contains("not found")) {
            userMessage = "❌ Path Not Found\n\n" +
                "The directory you selected doesn't exist.\n\n" +
                "How to fix this:\n" +
                "1. Create the directory first\n" +
                "2. Or select an existing directory\n" +
                "3. Try again";

        } else if (errorMessage.contains("Disk full") || errorMessage.contains("No space")) {
            userMessage = "❌ Disk Space Full\n\n" +
                "Your disk doesn't have enough space.\n\n" +
                "What to do:\n" +
                "1. Free up some disk space\n" +
                "2. Delete unnecessary files\n" +
                "3. Try again";

        } else {
            userMessage = "❌ Repository Initialization Failed\n\n" +
                "Could not initialize the repository.\n\n" +
                "Error Details:\n" +
                errorMessage + "\n\n" +
                "What to try:\n" +
                "1. Select a different directory\n" +
                "2. Check folder permissions\n" +
                "3. Make sure the path is valid\n" +
                "4. Ensure sufficient disk space";
        }

        return userMessage;
    }
}