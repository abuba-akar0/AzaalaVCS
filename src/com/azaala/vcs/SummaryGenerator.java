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

        int fileCount = commit.getFileCount();

        if (previousCommit == null) {
            return "Initial commit with " + fileCount + " file" + (fileCount != 1 ? "s" : "");
        }

        int previousFileCount = previousCommit.getFileCount();
        int difference = fileCount - previousFileCount;

        StringBuilder summary = new StringBuilder();

        // Basic file count summary
        if (difference > 0) {
            summary.append("Added ").append(difference).append(" file")
                  .append(difference != 1 ? "s" : "")
                  .append(" (total: ").append(fileCount).append(")");
        } else if (difference < 0) {
            summary.append("Removed ").append(Math.abs(difference)).append(" file")
                  .append(Math.abs(difference) != 1 ? "s" : "")
                  .append(" (total: ").append(fileCount).append(")");
        } else {
            summary.append("Modified ").append(fileCount).append(" file")
                  .append(fileCount != 1 ? "s" : "");
        }

        // Identify file types
        int codeFiles = countFilesByType(commit.getChangedFiles(), "java", "py", "cpp", "js");
        int docFiles = countFilesByType(commit.getChangedFiles(), "md", "txt", "pdf", "doc");
        int configFiles = countFilesByType(commit.getChangedFiles(), "xml", "json", "yml", "properties");

        if (codeFiles > 0 || docFiles > 0 || configFiles > 0) {
            summary.append("\nTypes: ");
            if (codeFiles > 0) summary.append("code (").append(codeFiles).append(") ");
            if (docFiles > 0) summary.append("docs (").append(docFiles).append(") ");
            if (configFiles > 0) summary.append("config (").append(configFiles).append(") ");
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
        for (String file : files) {
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
        for (int i = commits.size() - 1; i >= commits.size() - count; i--) {
            Commit commit = commits.get(i);
            summary.append("• ").append(commit.getCommitId())
                    .append(" - ").append(commit.getMessage())
                    .append(" (").append(commit.getTimestamp().format(FORMATTER))
                    .append(")\n");
            summary.append("  ").append(commit.getSummary()).append("\n\n");
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
        List<Commit> commits = repository.getCommits();

        return "Quick Status: " + commits.size() + " commits, " +
                stagedFiles.size() + " staged files";
    }
}
