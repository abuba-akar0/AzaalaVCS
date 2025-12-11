package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.persistence.dao.CommitDAO;
import com.azaala.vcs.persistence.dao.CommitFileDAO;
import com.azaala.vcs.persistence.models.CommitEntity;
import com.azaala.vcs.persistence.models.CommitFileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * DiffWorker - Compares commits and generates diff with database lookup
 */
public class DiffWorker extends BaseVCSWorker<List<String>> {
    private final VCS vcs;
    private final Repository repository;
    private final String commitId1;
    private final String commitId2;
    private final CommitDAO commitDAO;
    private final CommitFileDAO commitFileDAO;

    public DiffWorker(VCS vcs, Repository repository, String commitId1, String commitId2, ProgressListener progressListener) {
        super(progressListener);
        this.vcs = vcs;
        this.repository = repository;
        this.commitId1 = commitId1;
        this.commitId2 = commitId2;
        this.commitDAO = new CommitDAO();
        this.commitFileDAO = new CommitFileDAO();
    }

    public DiffWorker(VCS vcs, Repository repository, String commitId1, String commitId2) {
        this(vcs, repository, commitId1, commitId2, null);
    }

    @Override
    protected List<String> doInBackground() throws Exception {
        try {
            List<String> diffList = new ArrayList<>();

            publishProgress("Validating commits...", 10);

            // Check if commits exist in database
            CommitEntity commit1 = null;
            CommitEntity commit2 = null;

            if (commitId1 != null && !commitId1.isEmpty()) {
                commit1 = commitDAO.findById(commitId1);
                if (commit1 == null) {
                    publishProgress("Commit 1 not found in database, using VCS diff", 20);
                }
            }

            if (commitId2 != null && !commitId2.isEmpty()) {
                commit2 = commitDAO.findById(commitId2);
                if (commit2 == null) {
                    publishProgress("Commit 2 not found in database, using VCS diff", 20);
                }
            }

            publishProgress("Comparing commits...", 40);

            // Get VCS diff
            List<String> differences = vcs.diff(commitId1, commitId2);

            // Enhance with database information if available
            if (commit1 != null && commit2 != null) {
                publishProgress("Loading file details from database...", 60);

                diffList.add("=== Diff between commits ===\n");
                diffList.add("From: " + commit1.getCommitId() + " (" + commit1.getTimestamp() + ")");
                diffList.add("To: " + commit2.getCommitId() + " (" + commit2.getTimestamp() + ")");
                diffList.add("");

                // Get files from both commits
                List<CommitFileEntity> files1 = commitFileDAO.getCommitFiles(commitId1);
                List<CommitFileEntity> files2 = commitFileDAO.getCommitFiles(commitId2);

                diffList.add("Files in Commit 1: " + (files1 != null ? files1.size() : 0));
                if (files1 != null && !files1.isEmpty()) {
                    for (CommitFileEntity file : files1) {
                        diffList.add("  - " + file.getFilePath() + " (" + formatFileSize(file.getFileSize()) + ")");
                    }
                }

                diffList.add("");
                diffList.add("Files in Commit 2: " + (files2 != null ? files2.size() : 0));
                if (files2 != null && !files2.isEmpty()) {
                    for (CommitFileEntity file : files2) {
                        diffList.add("  - " + file.getFilePath() + " (" + formatFileSize(file.getFileSize()) + ")");
                    }
                }

                diffList.add("");
                diffList.add("=== VCS Diff Output ===");
            }

            // Add VCS diff
            if (differences != null && !differences.isEmpty()) {
                diffList.addAll(differences);
            } else {
                diffList.add("No differences found");
            }

            publishProgress("Diff generation complete", 100);
            return diffList;

        } catch (Exception e) {
            System.err.println("✗ Diff generation failed: " + e.getMessage());
            // Return VCS diff as fallback
            try {
                return vcs.diff(commitId1, commitId2);
            } catch (Exception ex) {
                throw e;
            }
        }
    }

    @Override
    protected void onSuccess(List<String> result) {
        if (result != null) {
            System.out.println("✓ Diff generation completed (" + result.size() + " lines)");
        } else {
            System.out.println("✓ Diff generation completed");
        }
    }

    @Override
    protected void onError(Exception exception) {
        System.err.println("✗ Diff generation error: " + exception.getMessage());
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
