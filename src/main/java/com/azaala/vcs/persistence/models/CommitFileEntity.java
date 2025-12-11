package com.azaala.vcs.persistence.models;

import java.time.LocalDateTime;

/**
 * Entity class representing a Commit File in the database.
 * Maps to the 'commit_files' table.
 * Represents files that were included in a specific commit.
 */
public class CommitFileEntity {
    private Long commitFileId;
    private String commitId;
    private Long repoId;
    private String filePath;
    private long fileSize;
    private String status;
    private LocalDateTime createdAt;

    public CommitFileEntity() {
    }

    public CommitFileEntity(String commitId, Long repoId, String filePath) {
        this.commitId = commitId;
        this.repoId = repoId;
        this.filePath = filePath;
        this.status = "added";
        this.createdAt = LocalDateTime.now();
    }

    public CommitFileEntity(Long commitFileId, String commitId, Long repoId, String filePath,
                           long fileSize, String status, LocalDateTime createdAt) {
        this.commitFileId = commitFileId;
        this.commitId = commitId;
        this.repoId = repoId;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getCommitFileId() { return commitFileId; }
    public void setCommitFileId(Long commitFileId) { this.commitFileId = commitFileId; }

    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "CommitFileEntity{" + "commitFileId=" + commitFileId + ", commitId='" + commitId + '\'' +
                ", repoId=" + repoId + ", filePath='" + filePath + '\'' + ", fileSize=" + fileSize +
                ", status='" + status + '\'' + ", createdAt=" + createdAt + '}';
    }
}

