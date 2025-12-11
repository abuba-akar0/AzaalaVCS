package com.azaala.vcs.persistence.models;

import java.time.LocalDateTime;

/**
 * Entity class representing a Staged File in the database.
 * Maps to the 'staged_files' table.
 */
public class StagedFileEntity {
    private Long stagedFileId;
    private Long repoId;
    private String filePath;
    private long fileSize;
    private LocalDateTime lastModified;
    private String status;
    private LocalDateTime createdAt;

    public StagedFileEntity() {
    }

    public StagedFileEntity(Long repoId, String filePath) {
        this.repoId = repoId;
        this.filePath = filePath;
        this.status = "staged";
        this.createdAt = LocalDateTime.now();
    }

    public StagedFileEntity(Long stagedFileId, Long repoId, String filePath, long fileSize,
                           LocalDateTime lastModified, String status, LocalDateTime createdAt) {
        this.stagedFileId = stagedFileId;
        this.repoId = repoId;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getStagedFileId() { return stagedFileId; }
    public void setStagedFileId(Long stagedFileId) { this.stagedFileId = stagedFileId; }

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "StagedFileEntity{" + "stagedFileId=" + stagedFileId + ", repoId=" + repoId +
                ", filePath='" + filePath + '\'' + ", fileSize=" + fileSize +
                ", lastModified=" + lastModified + ", status='" + status + '\'' +
                ", createdAt=" + createdAt + '}';
    }
}

