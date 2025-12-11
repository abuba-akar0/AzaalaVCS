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
        System.err.println("✗ File addition error: " + exception.getMessage());
        exception.printStackTrace();
    }
}
