package com.azaala.vcs.persistence.models;

import java.time.LocalDateTime;

/**
 * Entity class representing a Commit in the database.
 * Maps to the 'commits' table.
 */
public class CommitEntity {
    private String commitId;
    private Long repoId;
    private String message;
    private String summary;
    private String author;
    private LocalDateTime timestamp;
    private int fileCount;
    private LocalDateTime createdAt;

    public CommitEntity() {
    }

    public CommitEntity(String commitId, Long repoId, String message, String summary) {
        this.commitId = commitId;
        this.repoId = repoId;
        this.message = message;
        this.summary = summary;
        this.author = "Unknown";
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public CommitEntity(String commitId, Long repoId, String message, String summary,
                       String author, LocalDateTime timestamp, int fileCount, LocalDateTime createdAt) {
        this.commitId = commitId;
        this.repoId = repoId;
        this.message = message;
        this.summary = summary;
        this.author = author;
        this.timestamp = timestamp;
        this.fileCount = fileCount;
        this.createdAt = createdAt;
    }

    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "CommitEntity{" + "commitId='" + commitId + '\'' + ", repoId=" + repoId +
                ", message='" + message + '\'' + ", summary='" + summary + '\'' +
                ", author='" + author + '\'' + ", timestamp=" + timestamp +
                ", fileCount=" + fileCount + ", createdAt=" + createdAt + '}';
    }
}

