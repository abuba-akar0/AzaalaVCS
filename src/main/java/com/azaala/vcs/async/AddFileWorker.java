package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.persistence.dao.StagedFileDAO;
import com.azaala.vcs.persistence.dao.ActivityLogDAO;
import com.azaala.vcs.persistence.models.StagedFileEntity;
import com.azaala.vcs.persistence.models.ActivityLogEntity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * AddFileWorker - Stages files for commit with database integration
 */
public class AddFileWorker extends BaseVCSWorker<Boolean> {
    private final VCS vcs;
    private final Repository repository;
    private final String filePath;
    private final StagedFileDAO stagedFileDAO;
    private final ActivityLogDAO activityLogDAO;

    public AddFileWorker(VCS vcs, Repository repository, String filePath, ProgressListener progressListener) {
        super(progressListener);
        this.vcs = vcs;
        this.repository = repository;
        this.filePath = filePath;
        this.stagedFileDAO = new StagedFileDAO();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public AddFileWorker(VCS vcs, Repository repository, String filePath) {
        this(vcs, repository, filePath, null);
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            publishProgress("Validating file...", 10);
            File file = new File(filePath);
            if (!file.exists()) {
                throw new Exception("File does not exist: " + filePath);
            }

            publishProgress("Adding file to VCS: " + file.getName(), 25);
            boolean vcsResult = vcs.addFile(filePath);
            if (!vcsResult) {
                throw new Exception("Failed to add file in VCS");
            }

            if (repository != null && repository.getRepoId() != null) {
                publishProgress("Recording file in database...", 50);

                // Get file size
                long fileSize = Files.size(Paths.get(filePath));
                LocalDateTime lastModified = LocalDateTime.now();

                // Create staged file entity
                StagedFileEntity stagedFile = new StagedFileEntity(
                    repository.getRepoId(),
                    filePath
                );
                stagedFile.setFileSize(fileSize);
                stagedFile.setLastModified(lastModified);
                stagedFile.setStatus("staged");
                stagedFile.setCreatedAt(LocalDateTime.now());

                // Save to database
                Long fileId = stagedFileDAO.create(stagedFile);
                if (fileId == null) {
                    throw new Exception("Failed to save file to database");
                }

                publishProgress("Logging file addition...", 75);

                // Log activity
                ActivityLogEntity activityLog = new ActivityLogEntity(
                    repository.getRepoId(),
                    "ADD_FILE",
                    "Added file: " + filePath + " (Size: " + fileSize + " bytes)"
                );
                activityLogDAO.create(activityLog);
            }

            publishProgress("File added successfully", 100);
            return true;

        } catch (Exception e) {
            System.err.println("✗ File addition failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void onSuccess(Boolean result) {
        if (result) {
            System.out.println("✓ File addition completed successfully");
        } else {
            System.out.println("✗ File addition failed");
        }
    }

    @Override
    protected void onError(Exception exception) {
        String errorMessage = exception.getMessage();

        // Show ONLY user-friendly error dialog (no technical console output)
        showErrorDialog(errorMessage);
    }

    /**
     * Display user-friendly error dialog for file operations
     */
    private void showErrorDialog(String errorMessage) {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                String title = "File Addition Failed";
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
     * Build user-friendly error message for file operations
     */
    private String buildUserFriendlyMessage(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "Unknown error occurred";
        }

        String userMessage = "";

        if (errorMessage.contains("File does not exist")) {
            userMessage = "❌ File Not Found\n\n" +
                "The file you selected no longer exists.\n\n" +
                "Possible causes:\n" +
                "• File was deleted or moved\n" +
                "• File path is incorrect\n" +
                "• Network drive disconnected\n\n" +
                "What to do:\n" +
                "1. Check if the file still exists\n" +
                "2. Try browsing to the file again\n" +
                "3. Make sure all network drives are connected";

        } else if (errorMessage.contains("Path is not a file")) {
            userMessage = "❌ Invalid Selection\n\n" +
                "You selected a folder instead of a file.\n\n" +
                "To add files:\n" +
                "1. Click 'Add File' to select individual files\n" +
                "2. Or click 'Add All Files' to add entire folder\n" +
                "3. Select a file, not a folder";

        } else if (errorMessage.contains("Cannot read file") || errorMessage.contains("Permission denied")) {
            userMessage = "❌ Permission Denied\n\n" +
                "You don't have permission to access this file.\n\n" +
                "How to fix this:\n" +
                "1. Check file permissions\n" +
                "2. Make sure the file is readable\n" +
                "3. Run the application with proper permissions\n" +
                "4. Close any applications using this file\n\n" +
                "📍 If this persists, contact your system administrator";

        } else if (errorMessage.contains("outside repository")) {
            userMessage = "❌ File Outside Repository\n\n" +
                "This file is outside your repository directory.\n\n" +
                "Repository Rules:\n" +
                "• Files must be in or near the repository folder\n" +
                "• You can add files from the same directory level\n\n" +
                "What to do:\n" +
                "1. Select files within your repository\n" +
                "2. Or move the file to your repository\n" +
                "3. Then try adding it again";

        } else if (errorMessage.contains("Invalid file path")) {
            userMessage = "❌ Invalid File Path\n\n" +
                "The file path contains invalid characters.\n\n" +
                "Avoid using:\n" +
                "• Special characters: < > : \" | ? *\n" +
                "• Control characters\n" +
                "• Non-ASCII characters in some cases\n\n" +
                "What to do:\n" +
                "1. Rename the file with valid characters\n" +
                "2. Then try adding it again";

        } else if (errorMessage.contains("already added")) {
            userMessage = "ℹ️ File Already Staged\n\n" +
                "This file is already in your staging area.\n\n" +
                "Actions:\n" +
                "• The file is ready to be committed\n" +
                "• Select other files if you want to add more\n" +
                "• Click 'Commit' when you're ready";

        } else {
            userMessage = "❌ File Addition Failed\n\n" +
                "An error occurred while adding the file.\n\n" +
                "Error Details:\n" +
                errorMessage + "\n\n" +
                "What to try:\n" +
                "1. Check if the file exists\n" +
                "2. Verify file permissions\n" +
                "3. Try with a different file\n" +
                "4. Restart the application if needed";
        }

        return userMessage;
    }
}
