package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.persistence.dao.CommitDAO;
import com.azaala.vcs.persistence.dao.CommitFileDAO;
import com.azaala.vcs.persistence.models.CommitEntity;
import com.azaala.vcs.persistence.models.CommitFileEntity;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * HistoryLoadWorker - Loads commit history from database
 */
public class HistoryLoadWorker extends BaseVCSWorker<List<String>> {
    private final VCS vcs;
    private final Repository repository;
    private final int limit;
    private final CommitDAO commitDAO;
    private final CommitFileDAO commitFileDAO;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HistoryLoadWorker(VCS vcs, Repository repository, int limit, ProgressListener progressListener) {
        super(progressListener);
        this.vcs = vcs;
        this.repository = repository;
        this.limit = limit;
        this.commitDAO = new CommitDAO();
        this.commitFileDAO = new CommitFileDAO();
    }

    public HistoryLoadWorker(VCS vcs, Repository repository, int limit) {
        this(vcs, repository, limit, null);
    }

    public HistoryLoadWorker(VCS vcs, Repository repository, ProgressListener progressListener) {
        this(vcs, repository, Integer.MAX_VALUE, progressListener);
    }

    public HistoryLoadWorker(VCS vcs, Repository repository) {
        this(vcs, repository, Integer.MAX_VALUE, null);
    }

    @Override
    protected List<String> doInBackground() throws Exception {
        try {
            List<String> historyList = new ArrayList<>();

            publishProgress("Loading VCS commit history...", 20);
            List<String> vcsHistory = vcs.log();
            if (vcsHistory != null && !vcsHistory.isEmpty()) {
                historyList.addAll(vcsHistory);
            }

            // If repository is available, load from database for enhanced details
            if (repository != null && repository.getRepoId() != null) {
                publishProgress("Loading commit history from database...", 40);

                List<CommitEntity> commits;
                if (limit < Integer.MAX_VALUE) {
                    commits = commitDAO.findLatestByRepoId(repository.getRepoId(), limit);
                } else {
                    commits = commitDAO.findByRepoId(repository.getRepoId());
                }

                if (commits != null && !commits.isEmpty()) {
                    publishProgress("Formatting " + commits.size() + " commits...", 60);

                    historyList.clear(); // Replace with detailed database version
                    int index = 1;
                    for (CommitEntity commit : commits) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("[").append(index++).append("] ");
                        sb.append("Commit: ").append(commit.getCommitId()).append("\n");
                        sb.append("    Author: ").append(commit.getAuthor()).append("\n");
                        sb.append("    Date: ").append(commit.getTimestamp().format(dateFormatter)).append("\n");
                        sb.append("    Message: ").append(commit.getMessage()).append("\n");

                        // Get file count
                        int fileCount = commitFileDAO.getCommitFileCount(commit.getCommitId());
                        sb.append("    Files: ").append(fileCount).append("\n");

                        historyList.add(sb.toString());
                    }
                }
            }

            publishProgress("History loaded successfully", 100);
            return historyList;

        } catch (Exception e) {
            System.err.println("✗ History loading failed: " + e.getMessage());
            // Return VCS history as fallback
            try {
                return vcs.log();
            } catch (Exception ex) {
                throw e;
            }
        }
    }

    @Override
    protected void onSuccess(List<String> result) {
        if (result != null) {
            System.out.println("✓ History loading completed (" + result.size() + " commits)");
        } else {
            System.out.println("✓ History loading completed (no commits)");
        }
    }

    @Override
    protected void onError(Exception exception) {
        System.err.println("✗ History loading error: " + exception.getMessage());
        exception.printStackTrace();
    }
}
