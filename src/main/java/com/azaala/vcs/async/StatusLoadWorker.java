package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.persistence.dao.StagedFileDAO;
import com.azaala.vcs.persistence.models.StagedFileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * StatusLoadWorker - Loads repository status from database and file system
 */
public class StatusLoadWorker extends BaseVCSWorker<List<String>> {
    private final VCS vcs;
    private final Repository repository;
    private final StagedFileDAO stagedFileDAO;

    public StatusLoadWorker(VCS vcs, Repository repository, ProgressListener progressListener) {
        super(progressListener);
        this.vcs = vcs;
        this.repository = repository;
        this.stagedFileDAO = new StagedFileDAO();
    }

    public StatusLoadWorker(VCS vcs, Repository repository) {
        this(vcs, repository, null);
    }

    @Override
    protected List<String> doInBackground() throws Exception {
        try {
            List<String> statusList = new ArrayList<>();

            publishProgress("Scanning repository...", 20);
            List<String> vcsStatus = vcs.getStatus();
            if (vcsStatus != null) {
                statusList.addAll(vcsStatus);
            }

            if (repository != null && repository.getRepoId() != null) {
                publishProgress("Checking staged files from database...", 50);

                // Get staged files from database
                List<StagedFileEntity> stagedFiles = stagedFileDAO.findByRepoId(repository.getRepoId());
                if (stagedFiles != null && !stagedFiles.isEmpty()) {
                    publishProgress("Loading " + stagedFiles.size() + " staged files...", 70);
                    for (StagedFileEntity file : stagedFiles) {
                        statusList.add("[STAGED] " + file.getFilePath() + " (" + formatFileSize(file.getFileSize()) + ")");
                    }
                }
            }

            publishProgress("Status loaded successfully", 100);
            return statusList;

        } catch (Exception e) {
            System.err.println("✗ Status loading failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void onSuccess(List<String> result) {
        if (result != null) {
            System.out.println("✓ Status loading completed (" + result.size() + " items)");
        } else {
            System.out.println("✓ Status loading completed (no changes)");
        }
    }

    @Override
    protected void onError(Exception exception) {
        System.err.println("✗ Status loading error: " + exception.getMessage());
        exception.printStackTrace();
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int unitIndex = (int) (Math.log10(bytes) / Math.log10(1024));
        double displaySize = bytes / Math.pow(1024, unitIndex);
        return String.format("%.1f %s", displaySize, units[unitIndex]);
    }
}
