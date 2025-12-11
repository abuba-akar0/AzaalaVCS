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
        System.err.println("✗ Repository initialization error: " + exception.getMessage());
        exception.printStackTrace();
    }
}