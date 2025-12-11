package com.azaala.vcs.persistence.models;

import java.time.LocalDateTime;

/**
 * Entity class representing an Activity Log entry in the database.
 * Maps to the 'activity_logs' table.
 */
public class ActivityLogEntity {
    private Long logId;
    private Long repoId;
    private String operation;
    private String details;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;

    public ActivityLogEntity() {
    }

    public ActivityLogEntity(Long repoId, String operation, String details) {
        this.repoId = repoId;
        this.operation = operation;
        this.details = details;
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public ActivityLogEntity(Long logId, Long repoId, String operation, String details,
                            LocalDateTime timestamp, LocalDateTime createdAt) {
        this.logId = logId;
        this.repoId = repoId;
        this.operation = operation;
        this.details = details;
        this.timestamp = timestamp;
        this.createdAt = createdAt;
    }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Long getRepoId() { return repoId; }
    public void setRepoId(Long repoId) { this.repoId = repoId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ActivityLogEntity{" + "logId=" + logId + ", repoId=" + repoId +
                ", operation='" + operation + '\'' + ", details='" + details + '\'' +
                ", timestamp=" + timestamp + ", createdAt=" + createdAt + '}';
    }
}

