package com.azaala.vcs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a commit in the version control system.
 * A commit contains metadata and represents a snapshot of files.
 */
public class Commit {
    private String commitId;
    private String message;
    private String summary;
    private LocalDateTime timestamp;
    private List<String> changedFiles;

    /**
     * Creates a new Commit with the given message and files.
     *
     * @param message The commit message (cannot be null or empty)
     * @param changedFiles List of files included in this commit (cannot be null)
     * @throws IllegalArgumentException if message is null/empty or changedFiles is null
     */
    public Commit(String message, List<String> changedFiles) {
        validateMessage(message);
        validateChangedFiles(changedFiles);

        this.message = message.trim();
        this.timestamp = LocalDateTime.now();
        this.changedFiles = new ArrayList<>(changedFiles);

        // Generate a default summary
        this.summary = generateDefaultSummary(changedFiles.size());

        // Auto-generate ID upon creation for consistency
        this.commitId = generateUniqueId();
    }

    /**
     * Creates a new Commit with specific ID, message, timestamp and files.
     * This constructor is used by VCS for creating commits with pre-determined values.
     *
     * @param commitId The commit ID
     * @param message The commit message
     * @param timestamp The commit timestamp
     * @param changedFiles List of files included in this commit
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Commit(String commitId, String message, LocalDateTime timestamp, List<String> changedFiles) {
        if (commitId == null || commitId.trim().isEmpty()) {
            throw new IllegalArgumentException("Commit ID cannot be null or empty");
        }
        validateMessage(message);
        validateChangedFiles(changedFiles);
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        this.commitId = commitId.trim();
        this.message = message.trim();
        this.timestamp = timestamp;
        this.changedFiles = new ArrayList<>(changedFiles);

        // Generate a default summary
        this.summary = generateDefaultSummary(changedFiles.size());
    }

    /**
     * Creates a new Commit with the given message, files and summary.
     *
     * @param message The commit message (cannot be null or empty)
     * @param changedFiles List of files included in this commit (cannot be null)
     * @param summary A summary of the changes (cannot be null)
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public Commit(String message, List<String> changedFiles, String summary) {
        this(message, changedFiles);
        validateSummary(summary);
        this.summary = summary.trim();
    }

    /**
     * Validates commit message.
     *
     * @param message The message to validate
     * @throws IllegalArgumentException if message is invalid
     */
    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Commit message cannot be null or empty");
        }

        // Add maximum length check
        if (message.trim().length() > 500) {
            throw new IllegalArgumentException("Commit message too long (max 500 characters)");
        }
    }

    /**
     * Validates changed files list.
     *
     * @param changedFiles The list to validate
     * @throws IllegalArgumentException if list is null
     */
    private void validateChangedFiles(List<String> changedFiles) {
        if (changedFiles == null) {
            throw new IllegalArgumentException("Changed files list cannot be null");
        }
    }

    /**
     * Validates summary.
     *
     * @param summary The summary to validate
     * @throws IllegalArgumentException if summary is null
     */
    private void validateSummary(String summary) {
        if (summary == null) {
            throw new IllegalArgumentException("Summary cannot be null");
        }
    }

    /**
     * Generates a default summary based on the number of files.
     *
     * @param fileCount Number of files changed
     * @return Generated summary string
     */
    private String generateDefaultSummary(int fileCount) {
        if (fileCount == 0) {
            return "No files changed";
        } else if (fileCount == 1) {
            return "Changed 1 file";
        } else {
            return "Changed " + fileCount + " files";
        }
    }

    /**
     * Generates a unique ID for this commit using UUID.
     *
     * @return The generated commit ID
     */
    private String generateUniqueId() {
        // Use random UUID for better uniqueness and security
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Gets the commit ID. The ID is auto-generated during object creation.
     *
     * @return The commit ID (never null)
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * Sets the commit ID.
     *
     * @param commitId The new commit ID
     * @throws IllegalArgumentException if commitId is null or empty
     */
    public void setCommitId(String commitId) {
        if (commitId == null || commitId.trim().isEmpty()) {
            throw new IllegalArgumentException("Commit ID cannot be null or empty");
        }
        this.commitId = commitId.trim();
    }

    /**
     * Gets the commit message.
     *
     * @return The commit message (never null)
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the commit message.
     *
     * @param message The new commit message
     * @throws IllegalArgumentException if message is null or empty
     */
    public void setMessage(String message) {
        validateMessage(message);
        this.message = message.trim();
    }

    /**
     * Gets the commit summary.
     *
     * @return The summary of changes (never null)
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the commit summary.
     *
     * @param summary The new summary
     * @throws IllegalArgumentException if summary is null
     */
    public void setSummary(String summary) {
        validateSummary(summary);
        this.summary = summary.trim();
    }

    /**
     * Gets the commit timestamp.
     *
     * @return The timestamp when the commit was created (never null)
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the commit timestamp.
     *
     * @param timestamp The new timestamp
     * @throws IllegalArgumentException if timestamp is null
     */
    public void setTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        this.timestamp = timestamp;
    }

    /**
     * Gets the list of changed files in this commit.
     * Returns a defensive copy to prevent external modification.
     *
     * @return Immutable copy of files included in this commit (never null)
     */
    public List<String> getChangedFiles() {
        return new ArrayList<>(changedFiles);
    }

    /**
     * Sets the list of changed files.
     *
     * @param changedFiles The new list of changed files
     * @throws IllegalArgumentException if changedFiles is null
     */
    public void setChangedFiles(List<String> changedFiles) {
        validateChangedFiles(changedFiles);
        this.changedFiles = new ArrayList<>(changedFiles);

        // Update summary when files change
        this.summary = generateDefaultSummary(changedFiles.size());
    }

    /**
     * Retrieves the full file path from the commit based on the file name.
     * Performs case-sensitive matching.
     *
     * @param fileName The name of the file to search for
     * @return The full file path if found, otherwise null
     * @throws IllegalArgumentException if fileName is null or empty
     */
    public String getFileFromCommit(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        String trimmedFileName = fileName.trim();

        for (String filePath : changedFiles) {
            if (filePath != null && filePath.endsWith(trimmedFileName)) {
                return filePath;
            }
        }
        return null;
    }

    /**
     * Checks if this commit contains a specific file.
     *
     * @param filePath The file path to check
     * @return true if the file is included in this commit, false otherwise
     */
    public boolean containsFile(String filePath) {
        if (filePath == null) {
            return false;
        }
        return changedFiles.contains(filePath.trim());
    }

    /**
     * Gets the number of files changed in this commit.
     *
     * @return Number of changed files
     */
    public int getFileCount() {
        return changedFiles.size();
    }

    /**
     * Checks if this commit has any changed files.
     *
     * @return true if there are changed files, false otherwise
     */
    public boolean hasChangedFiles() {
        return !changedFiles.isEmpty();
    }

    /**
     * Returns a string representation of the commit.
     *
     * @return A string containing commit details
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Commit " + commitId +
                " (" + timestamp.format(formatter) + ")\n" +
                "Message: " + message + "\n" +
                "Summary: " + summary + "\n" +
                "Files: " + changedFiles.size();
    }

    /**
     * Checks equality based on commit ID.
     *
     * @param obj Object to compare with
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Commit commit = (Commit) obj;
        return Objects.equals(commitId, commit.commitId);
    }

    /**
     * Generates hash code based on commit ID.
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(commitId);
    }

    /**
     * Gets a formatted string with all commit details.
     *
     * @param detailed Whether to include file details
     * @return Formatted commit information
     */
    public String getFormattedInfo(boolean detailed) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder info = new StringBuilder();

        info.append("Commit: ").append(commitId).append("\n");
        info.append("Date: ").append(timestamp.format(formatter)).append("\n");
        info.append("Message: ").append(message).append("\n");
        info.append("Summary: ").append(summary).append("\n");
        info.append("Files: ").append(changedFiles.size());

        if (detailed && !changedFiles.isEmpty()) {
            info.append("\n\nChanged files:");
            for (String file : changedFiles) {
                info.append("\n  - ").append(file);
            }
        }

        return info.toString();
    }

    /**
     * Converts this commit to a Map representation.
     * This is useful for serialization and testing.
     *
     * @return Map containing the commit's properties
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("commitId", commitId);
        map.put("message", message);
        map.put("summary", summary);
        map.put("timestamp", timestamp);
        map.put("changedFiles", new ArrayList<>(changedFiles));
        map.put("fileCount", changedFiles.size());
        return map;
    }
}
