package com.azaala.vcs.persistence.dao;

import com.azaala.vcs.persistence.DatabaseException;
import com.azaala.vcs.persistence.models.CommitEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Commit entities.
 * Handles CRUD operations for commits in the database.
 */
public class CommitDAO extends BaseDAO {

    public boolean create(CommitEntity commit) throws DatabaseException {
        String sql = "INSERT INTO commits (commit_id, repo_id, message, summary, author, timestamp, file_count, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commit.getCommitId());
            stmt.setLong(2, commit.getRepoId());
            stmt.setString(3, commit.getMessage());
            stmt.setString(4, commit.getSummary());
            stmt.setString(5, commit.getAuthor());
            stmt.setTimestamp(6, Timestamp.valueOf(commit.getTimestamp()));
            stmt.setInt(7, commit.getFileCount());
            stmt.setTimestamp(8, Timestamp.valueOf(commit.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();
            logOperation("CREATE_COMMIT", "Commit ID: " + commit.getCommitId() + ", Repo ID: " + commit.getRepoId());
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error creating commit", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    public CommitEntity findById(String commitId) throws DatabaseException {
        String sql = "SELECT * FROM commits WHERE commit_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commitId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } catch (SQLException e) {
            handleSQLException("Error finding commit by ID", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return null;
    }

    public List<CommitEntity> findByRepoId(Long repoId) throws DatabaseException {
        String sql = "SELECT * FROM commits WHERE repo_id = ? ORDER BY timestamp DESC";
        List<CommitEntity> commits = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                commits.add(mapResultSetToEntity(rs));
            }
            return commits;
        } catch (SQLException e) {
            handleSQLException("Error finding commits by repository ID", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return commits;
    }

    public List<CommitEntity> findLatestByRepoId(Long repoId, int limit) throws DatabaseException {
        String sql = "SELECT * FROM commits WHERE repo_id = ? ORDER BY timestamp DESC LIMIT ?";
        List<CommitEntity> commits = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                commits.add(mapResultSetToEntity(rs));
            }
            return commits;
        } catch (SQLException e) {
            handleSQLException("Error finding latest commits", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return commits;
    }

    public int countByRepoId(Long repoId) throws DatabaseException {
        String sql = "SELECT COUNT(*) as total FROM commits WHERE repo_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            handleSQLException("Error counting commits", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    public boolean update(CommitEntity commit) throws DatabaseException {
        String sql = "UPDATE commits SET message = ?, summary = ?, author = ?, file_count = ? WHERE commit_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commit.getMessage());
            stmt.setString(2, commit.getSummary());
            stmt.setString(3, commit.getAuthor());
            stmt.setInt(4, commit.getFileCount());
            stmt.setString(5, commit.getCommitId());

            int affectedRows = stmt.executeUpdate();
            logOperation("UPDATE_COMMIT", "Commit ID: " + commit.getCommitId());
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating commit", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    public boolean deleteById(String commitId) throws DatabaseException {
        String sql = "DELETE FROM commits WHERE commit_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, commitId);

            int affectedRows = stmt.executeUpdate();
            logOperation("DELETE_COMMIT", "Commit ID: " + commitId);
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting commit", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    /**
     * Get total count of all commits in the database
     */
    public int getCommitCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) as total FROM commits";
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
            handleSQLException("Error counting total commits", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    /**
     * Get the N most recent commits from all repositories
     */
    public List<CommitEntity> findRecent(int limit) throws DatabaseException {
        String sql = "SELECT * FROM commits ORDER BY timestamp DESC LIMIT ?";
        List<CommitEntity> commits = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                commits.add(mapResultSetToEntity(rs));
            }
            return commits;
        } catch (SQLException e) {
            handleSQLException("Error finding recent commits", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return commits;
    }

    /**
     * Get all commits from all repositories
     */
    public List<CommitEntity> findAll() throws DatabaseException {
        String sql = "SELECT * FROM commits ORDER BY timestamp DESC";
        List<CommitEntity> commits = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                commits.add(mapResultSetToEntity(rs));
            }
            return commits;
        } catch (SQLException e) {
            handleSQLException("Error finding all commits", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return commits;
    }

    private CommitEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        CommitEntity entity = new CommitEntity();
        entity.setCommitId(rs.getString("commit_id"));
        entity.setRepoId(rs.getLong("repo_id"));
        entity.setMessage(rs.getString("message"));
        entity.setSummary(rs.getString("summary"));
        entity.setAuthor(rs.getString("author"));
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setFileCount(rs.getInt("file_count"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return entity;
    }
}

