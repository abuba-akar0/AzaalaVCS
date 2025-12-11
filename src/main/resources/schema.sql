-- src/resources/schema.sql
-- Azaala VCS Database Schema (fixed index lengths for utf8mb4)

CREATE DATABASE IF NOT EXISTS azaala_vcs;
USE azaala_vcs;

CREATE TABLE IF NOT EXISTS repositories
(
    repo_id        BIGINT PRIMARY KEY AUTO_INCREMENT,
    repo_name      VARCHAR(255) NOT NULL UNIQUE,
    repo_path      VARCHAR(500) NOT NULL UNIQUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_commit_at TIMESTAMP    NULL,
    description    VARCHAR(1024),
    INDEX idx_repo_name (repo_name),
    INDEX idx_repo_path (repo_path),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS commits
(
    commit_id  VARCHAR(128) PRIMARY KEY,
    repo_id    BIGINT NOT NULL,
    message    TEXT   NOT NULL,
    summary    VARCHAR(512),
    author     VARCHAR(255) DEFAULT 'Unknown',
    timestamp  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    file_count INT          DEFAULT 0,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (repo_id) REFERENCES repositories (repo_id) ON DELETE CASCADE,
    INDEX idx_repo_id (repo_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_created_at (created_at),
    INDEX idx_repo_timestamp (repo_id, timestamp DESC)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS staged_files
(
    staged_file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    repo_id        BIGINT       NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    file_size      BIGINT      DEFAULT 0,
    last_modified  TIMESTAMP    NULL,
    status         VARCHAR(50) DEFAULT 'staged',
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (repo_id) REFERENCES repositories (repo_id) ON DELETE CASCADE,
    UNIQUE KEY unique_repo_file (repo_id, file_path),
    INDEX idx_repo_id (repo_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS commit_files
(
    commit_file_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    commit_id      VARCHAR(128) NOT NULL,
    repo_id        BIGINT       NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    file_size      BIGINT      DEFAULT 0,
    status         VARCHAR(50) DEFAULT 'added',
    created_at     TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (commit_id) REFERENCES commits (commit_id) ON DELETE CASCADE,
    FOREIGN KEY (repo_id) REFERENCES repositories (repo_id) ON DELETE CASCADE,
    INDEX idx_commit_id (commit_id),
    INDEX idx_repo_id (repo_id),
    INDEX idx_file_path (file_path),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS activity_logs
(
    log_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
    repo_id    BIGINT       NOT NULL,
    operation  VARCHAR(100) NOT NULL,
    details    TEXT,
    timestamp  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (repo_id) REFERENCES repositories (repo_id) ON DELETE CASCADE,
    INDEX idx_repo_id (repo_id),
    INDEX idx_operation (operation),
    INDEX idx_timestamp (timestamp),
    INDEX idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS database_config
(
    config_key   VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(500),
    description  VARCHAR(500),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT IGNORE INTO database_config (config_key, config_value, description)
VALUES ('schema_version', '2.0.0', 'Current database schema version'),
       ('hybrid_storage_enabled', 'true', 'Enable hybrid storage (DB + file system)'),
       ('enable_activity_logging', 'true', 'Enable activity logging for audit trail');

CREATE INDEX idx_commits_repo_timestamp ON commits (repo_id, timestamp DESC);
CREATE INDEX idx_commit_files_commit_repo ON commit_files (commit_id, repo_id);
CREATE INDEX idx_staged_files_repo_status ON staged_files (repo_id, status);
CREATE INDEX idx_activity_logs_repo_operation ON activity_logs (repo_id, operation);
