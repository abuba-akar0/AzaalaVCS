package com.azaala.vcs.persistence.dao;

import com.azaala.vcs.persistence.DatabaseException;
import com.azaala.vcs.persistence.models.CommitFileEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Commit File entities.
 * Handles CRUD operations for files included in commits.
 */
public class CommitFileDAO extends BaseDAO {

    /**
     * Add a file to a commit
     */
    public Long addFile(CommitFileEntity commitFile) throws DatabaseException {
        String sql = "INSERT INTO commit_files (commit_id, repo_id, file_path, file_size, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, commitFile.getCommitId());
            stmt.setLong(2, commitFile.getRepoId());
            stmt.setString(3, commitFile.getFilePath());
            stmt.setLong(4, commitFile.getFileSize());
            stmt.setString(5, commitFile.getStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Failed to add file to commit: no rows affected");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                logOperation("ADD_COMMIT_FILE", "File: " + commitFile.getFilePath() + ", Commit: " + commitFile.getCommitId());
                return id;
            } else {
                throw new DatabaseException("Failed to get generated commit file ID");
            }
        } catch (SQLException e) {
            handleSQLException("Error adding file to commit", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return null;
    }

    /**
     * Get all files in a commit
     */
    public List<CommitFileEntity> getCommitFiles(String commitId) throws DatabaseException {
        String sql = "SELECT * FROM commit_files WHERE commit_id = ? ORDER BY created_at ASC";
        List<CommitFileEntity> files = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commitId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                files.add(mapResultSetToEntity(rs));
            }
            return files;
        } catch (SQLException e) {
            handleSQLException("Error getting commit files", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return files;
    }

    /**
     * Get count of files in a commit
     */
    public int getCommitFileCount(String commitId) throws DatabaseException {
        String sql = "SELECT COUNT(*) as total FROM commit_files WHERE commit_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commitId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Error counting commit files", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    /**
     * Get count of all commit files in the database
     */
    public int getCommitFileCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) as total FROM commit_files";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Error counting total commit files", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    /**
     * Get all commit files from the database
     */
    public List<CommitFileEntity> findAll() throws DatabaseException {
        String sql = "SELECT * FROM commit_files ORDER BY created_at DESC";
        List<CommitFileEntity> files = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                files.add(mapResultSetToEntity(rs));
            }
            return files;
        } catch (SQLException e) {
            handleSQLException("Error finding all commit files", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return files;
    }

    /**
     * Get file history (all commits containing a specific file)
     */
    public List<CommitFileEntity> getFileHistory(String filePath) throws DatabaseException {
        String sql = "SELECT * FROM commit_files WHERE file_path = ? ORDER BY created_at DESC";
        List<CommitFileEntity> files = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, filePath);
            rs = stmt.executeQuery();

            while (rs.next()) {
                files.add(mapResultSetToEntity(rs));
            }
            return files;
        } catch (SQLException e) {
            handleSQLException("Error getting file history", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return files;
    }

    /**
     * Get files in a repository by commit ID
     */
    public List<CommitFileEntity> getFilesByRepoAndCommit(Long repoId, String commitId) throws DatabaseException {
        String sql = "SELECT * FROM commit_files WHERE repo_id = ? AND commit_id = ? ORDER BY file_path ASC";
        List<CommitFileEntity> files = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            stmt.setString(2, commitId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                files.add(mapResultSetToEntity(rs));
            }
            return files;
        } catch (SQLException e) {
            handleSQLException("Error getting files by repo and commit", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return files;
    }

    /**
     * Update file status in a commit
     */
    public boolean updateFileStatus(Long commitFileId, String status) throws DatabaseException {
        String sql = "UPDATE commit_files SET status = ? WHERE commit_file_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setLong(2, commitFileId);

            int affectedRows = stmt.executeUpdate();
            logOperation("UPDATE_COMMIT_FILE_STATUS", "Commit File ID: " + commitFileId + ", Status: " + status);
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating commit file status", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Delete a file from a commit
     */
    public boolean deleteFile(Long commitFileId) throws DatabaseException {
        String sql = "DELETE FROM commit_files WHERE commit_file_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, commitFileId);

            int affectedRows = stmt.executeUpdate();
            logOperation("DELETE_COMMIT_FILE", "Commit File ID: " + commitFileId);
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting commit file", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Get total file size in a commit
     */
    public long getTotalFileSizeInCommit(String commitId) throws DatabaseException {
        String sql = "SELECT SUM(file_size) as total_size FROM commit_files WHERE commit_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commitId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("total_size");
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Error calculating total file size", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    /**
     * Map ResultSet to CommitFileEntity
     */
    private CommitFileEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        CommitFileEntity entity = new CommitFileEntity();
        entity.setCommitFileId(rs.getLong("commit_file_id"));
        entity.setCommitId(rs.getString("commit_id"));
        entity.setRepoId(rs.getLong("repo_id"));
        entity.setFilePath(rs.getString("file_path"));
        entity.setFileSize(rs.getLong("file_size"));
        entity.setStatus(rs.getString("status"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return entity;
    }
}

