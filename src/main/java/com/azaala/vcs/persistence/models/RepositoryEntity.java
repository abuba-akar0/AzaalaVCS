package com.azaala.vcs.persistence.models;

import java.time.LocalDateTime;

/**
 * Entity class representing a Repository in the database.
 * Maps to the 'repositories' table.
 */
public class RepositoryEntity {
    private Long repoId;
    private String repoName;
    private String repoPath;
    private LocalDateTime createdAt;
    private LocalDateTime lastCommitAt;
    private String description;

    public RepositoryEntity() {
    }

    public RepositoryEntity(String repoName, String repoPath) {
        this.repoName = repoName;
        this.repoPath = repoPath;
        this.createdAt = LocalDateTime.now();
    }

    public RepositoryEntity(Long repoId, String repoName, String repoPath,
                            LocalDateTime createdAt, LocalDateTime lastCommitAt, String description) {
        this.repoId = repoId;
        this.repoName = repoName;
        this.repoPath = repoPath;
        this.createdAt = createdAt;
        this.lastCommitAt = lastCommitAt;
        this.description = description;
    }

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }

    public String getRepoName() { return repoName; }
    public void setRepoName(String repoName) { this.repoName = repoName; }

    public String getRepoPath() { return repoPath; }
    public void setRepoPath(String repoPath) { this.repoPath = repoPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastCommitAt() { return lastCommitAt; }
    public void setLastCommitAt(LocalDateTime lastCommitAt) { this.lastCommitAt = lastCommitAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "RepositoryEntity{" + "repoId=" + repoId + ", repoName='" + repoName + '\'' +
                ", repoPath='" + repoPath + '\'' + ", createdAt=" + createdAt +
                ", lastCommitAt=" + lastCommitAt + ", description='" + description + '\'' + '}';
    }
}