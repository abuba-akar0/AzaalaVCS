package com.azaala.vcs.persistence.dao;

import com.azaala.vcs.persistence.DatabaseException;
import com.azaala.vcs.persistence.models.StagedFileEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Staged File entities.
 * Handles CRUD operations for staged files in the database.
 */
public class StagedFileDAO extends BaseDAO {

    public Long create(StagedFileEntity stagedFile) throws DatabaseException {
        String sql = "INSERT INTO staged_files (repo_id, file_path, file_size, last_modified, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, stagedFile.getRepoId());
            stmt.setString(2, stagedFile.getFilePath());
            stmt.setLong(3, stagedFile.getFileSize());
            stmt.setTimestamp(4, stagedFile.getLastModified() != null ?
                    Timestamp.valueOf(stagedFile.getLastModified()) : null);
            stmt.setString(5, stagedFile.getStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Failed to create staged file: no rows affected");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                logOperation("CREATE_STAGED_FILE", "File: " + stagedFile.getFilePath() + ", Repo ID: " + stagedFile.getRepoId());
                return id;
            } else {
                throw new DatabaseException("Failed to get generated staged file ID");
            }
        } catch (SQLException e) {
            handleSQLException("Error creating staged file", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return null;
    }

    public StagedFileEntity findById(Long stagedFileId) throws DatabaseException {
        String sql = "SELECT * FROM staged_files WHERE staged_file_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, stagedFileId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } catch (SQLException e) {
            handleSQLException("Error finding staged file by ID", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return null;
    }

    public List<StagedFileEntity> findByRepoId(Long repoId) throws DatabaseException {
        String sql = "SELECT * FROM staged_files WHERE repo_id = ? AND status = 'staged' ORDER BY created_at DESC";
        List<StagedFileEntity> stagedFiles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                stagedFiles.add(mapResultSetToEntity(rs));
            }
            return stagedFiles;
        } catch (SQLException e) {
            handleSQLException("Error finding staged files by repository ID", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return stagedFiles;
    }

    public StagedFileEntity findByRepoIdAndPath(Long repoId, String filePath) throws DatabaseException {
        String sql = "SELECT * FROM staged_files WHERE repo_id = ? AND file_path = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            stmt.setString(2, filePath);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } catch (SQLException e) {
            handleSQLException("Error finding staged file by repo ID and path", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return null;
    }

    public boolean update(StagedFileEntity stagedFile) throws DatabaseException {
        String sql = "UPDATE staged_files SET file_size = ?, last_modified = ?, status = ? WHERE staged_file_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, stagedFile.getFileSize());
            stmt.setTimestamp(2, stagedFile.getLastModified() != null ?
                    Timestamp.valueOf(stagedFile.getLastModified()) : null);
            stmt.setString(3, stagedFile.getStatus());
            stmt.setLong(4, stagedFile.getStagedFileId());

            int affectedRows = stmt.executeUpdate();
            logOperation("UPDATE_STAGED_FILE", "Staged File ID: " + stagedFile.getStagedFileId());
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating staged file", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    public boolean deleteById(Long stagedFileId) throws DatabaseException {
        String sql = "DELETE FROM staged_files WHERE staged_file_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, stagedFileId);

            int affectedRows = stmt.executeUpdate();
            logOperation("DELETE_STAGED_FILE", "Staged File ID: " + stagedFileId);
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting staged file", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    public int deleteByRepoId(Long repoId) throws DatabaseException {
        String sql = "DELETE FROM staged_files WHERE repo_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);

            int affectedRows = stmt.executeUpdate();
            logOperation("DELETE_STAGED_FILES_BY_REPO", "Repo ID: " + repoId + ", Count: " + affectedRows);
            return affectedRows;
        } catch (SQLException e) {
            handleSQLException("Error deleting staged files by repository ID", e);
        } finally {
            closeResources(stmt, conn);
        }
        return 0;
    }

    /**
     * Get all staged files from all repositories
     */
    public List<StagedFileEntity> findAll() throws DatabaseException {
        String sql = "SELECT * FROM staged_files ORDER BY created_at DESC";
        List<StagedFileEntity> stagedFiles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                stagedFiles.add(mapResultSetToEntity(rs));
            }
            return stagedFiles;
        } catch (SQLException e) {
            handleSQLException("Error finding all staged files", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return stagedFiles;
    }

    private StagedFileEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        StagedFileEntity entity = new StagedFileEntity();
        entity.setStagedFileId(rs.getLong("staged_file_id"));
        entity.setRepoId(rs.getLong("repo_id"));
        entity.setFilePath(rs.getString("file_path"));
        entity.setFileSize(rs.getLong("file_size"));

        Timestamp lastModifiedTs = rs.getTimestamp("last_modified");
        if (lastModifiedTs != null) {
            entity.setLastModified(lastModifiedTs.toLocalDateTime());
        }

        entity.setStatus(rs.getString("status"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return entity;
    }
}
