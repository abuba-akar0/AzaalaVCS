package com.azaala.vcs;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates summaries for commits and repository activity.
 */
public class SummaryGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
            "(public|private|protected)?\\s+\\w+\\s+\\w+\\(.*\\)\\s*\\{?");
    private static final Pattern TODO_PATTERN = Pattern.compile("(TODO|FIXME):\\s*(.*)");

    /**
     * Generates a summary for a commit.
     *
     * @param commit Current commit
     * @param previousCommit Previous commit (can be null)
     * @return Generated summary
     */
    public String generateSummary(Commit commit, Commit previousCommit) {
        if (commit == null) {
            return "Invalid commit";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("=== Commit Summary ===\n");
        summary.append("ID: ").append(commit.getCommitId()).append("\n");
        summary.append("Message: ").append(commit.getMessage()).append("\n");
        if (commit.getTimestamp() != null) {
            summary.append("Time: ").append(commit.getTimestamp().format(FORMATTER)).append("\n");
        }
        summary.append("\n");

        int fileCount = commit.getFileCount();

        if (previousCommit == null) {
            summary.append("Type: Initial commit\n");
            summary.append("Files added: ").append(fileCount).append("\n");
        } else {
            int previousFileCount = previousCommit.getFileCount();
            int difference = fileCount - previousFileCount;

            if (difference > 0) {
                summary.append("Files added: ").append(difference).append(" (total: ").append(fileCount).append(")\n");
            } else if (difference < 0) {
                summary.append("Files removed: ").append(Math.abs(difference)).append(" (total: ").append(fileCount).append(")\n");
            } else {
                summary.append("Files modified (no count change): ").append(fileCount).append("\n");
            }
        }

        List<String> changedFiles = commit.getChangedFiles();
        if (changedFiles == null) {
            changedFiles = java.util.Collections.emptyList();
        }

        // Identify file types
        int codeFiles = countFilesByType(changedFiles, "java", "py", "cpp", "js");
        int docFiles = countFilesByType(changedFiles, "md", "txt", "pdf", "doc");
        int configFiles = countFilesByType(changedFiles, "xml", "json", "yml", "properties");

        summary.append("\nBreakdown:\n");
        summary.append(" - Code files: ").append(codeFiles).append("\n");
        summary.append(" - Docs: ").append(docFiles).append("\n");
        summary.append(" - Config: ").append(configFiles).append("\n");

        // List changed files (limited to avoid huge output)
        summary.append("\nChanged files (").append(changedFiles.size()).append("):\n");
        int limit = Math.min(changedFiles.size(), 20);
        for (int i = 0; i < limit; i++) {
            summary.append(" - ").append(changedFiles.get(i)).append("\n");
        }
        if (changedFiles.size() > limit) {
            summary.append(" - ... and ").append(changedFiles.size() - limit).append(" more\n");
        }

        return summary.toString();
    }

    /**
     * Counts files by their extensions.
     *
     * @param files List of file paths
     * @param extensions Extensions to match
     * @return Count of matching files
     */
    private int countFilesByType(List<String> files, String... extensions) {
        int count = 0;
        if (files == null) return 0;
        for (String file : files) {
            if (file == null) continue;
            String lowercaseFile = file.toLowerCase();
            for (String ext : extensions) {
                if (lowercaseFile.endsWith("." + ext)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    /**
     * Generates a commit summary for multiple commits.
     *
     * @param commits List of commits
     * @param limit Maximum number to include
     * @return Summary string
     */
    public String generateCommitSummary(List<Commit> commits, int limit) {
        if (commits == null || commits.isEmpty()) {
            return "No commits in repository";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("=== Recent Activity Summary ===\n");
        summary.append("Total commits: ").append(commits.size()).append("\n\n");

        int count = Math.min(limit, commits.size());
        // Show newest first
        for (int i = commits.size() - 1, shown = 0; i >= 0 && shown < count; i--, shown++) {
            Commit commit = commits.get(i);
            summary.append((shown + 1)).append(") ").append(commit.getCommitId())
                    .append(" - ").append(commit.getMessage()).append("\n");
            if (commit.getTimestamp() != null) {
                summary.append("   Date: ").append(commit.getTimestamp().format(FORMATTER)).append("\n");
            }
            String commitSummary = commit.getSummary();
            if (commitSummary != null && !commitSummary.isEmpty()) {
                summary.append("   Summary: ").append(commitSummary).append("\n");
            }
            summary.append("---\n");
        }

        return summary.toString();
    }

    /**
     * Generates a quick status summary.
     *
     * @param repository The repository
     * @return Quick status string
     */
    public String generateQuickStatus(Repository repository) {
        if (repository == null) {
            return "Repository not available";
        }

        List<String> stagedFiles = repository.getStagedFiles();
        if (stagedFiles == null) stagedFiles = java.util.Collections.emptyList();
        List<Commit> commits = repository.getCommits();
        if (commits == null) commits = java.util.Collections.emptyList();

        StringBuilder status = new StringBuilder();
        status.append("=== Quick Status ===\n");
        status.append("Commits: ").append(commits.size()).append("\n");
        status.append("Staged files: ").append(stagedFiles.size()).append("\n");

        int show = Math.min(stagedFiles.size(), 10);
        if (show > 0) {
            status.append("\nStaged files preview:\n");
            for (int i = 0; i < show; i++) {
                status.append(" - ").append(stagedFiles.get(i)).append("\n");
            }
            if (stagedFiles.size() > show) {
                status.append(" - ... and ").append(stagedFiles.size() - show).append(" more\n");
            }
        }

        if (!commits.isEmpty()) {
            Commit last = commits.get(commits.size() - 1);
            status.append("\nLast commit: ").append(last.getCommitId()).append(" - ").append(last.getMessage()).append("\n");
        }

        return status.toString();
    }
}
