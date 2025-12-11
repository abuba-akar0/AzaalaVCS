package com.azaala.vcs.persistence.dao;

import com.azaala.vcs.persistence.DatabaseException;
import com.azaala.vcs.persistence.models.ActivityLogEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Activity Log entities.
 * Handles CRUD operations for activity logs in the database.
 */
public class ActivityLogDAO extends BaseDAO {

    public Long create(ActivityLogEntity log) throws DatabaseException {
        String sql = "INSERT INTO activity_logs (repo_id, operation, details, timestamp, created_at) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, log.getRepoId());
            stmt.setString(2, log.getOperation());
            stmt.setString(3, log.getDetails());
            stmt.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Failed to create activity log: no rows affected");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                Long id = rs.getLong(1);
                return id;
            } else {
                throw new DatabaseException("Failed to get generated log ID");
            }
        } catch (SQLException e) {
            handleSQLException("Error creating activity log", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return null;
    }

    public List<ActivityLogEntity> findByRepoId(Long repoId) throws DatabaseException {
        String sql = "SELECT * FROM activity_logs WHERE repo_id = ? ORDER BY timestamp DESC";
        List<ActivityLogEntity> logs = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToEntity(rs));
            }
            return logs;
        } catch (SQLException e) {
            handleSQLException("Error finding activity logs by repository ID", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return logs;
    }

    public List<ActivityLogEntity> findLatestByRepoId(Long repoId, int limit) throws DatabaseException {
        String sql = "SELECT * FROM activity_logs WHERE repo_id = ? ORDER BY timestamp DESC LIMIT ?";
        List<ActivityLogEntity> logs = new ArrayList<>();
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
                logs.add(mapResultSetToEntity(rs));
            }
            return logs;
        } catch (SQLException e) {
            handleSQLException("Error finding latest activity logs", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return logs;
    }

    public List<ActivityLogEntity> findByOperation(Long repoId, String operation) throws DatabaseException {
        String sql = "SELECT * FROM activity_logs WHERE repo_id = ? AND operation = ? ORDER BY timestamp DESC";
        List<ActivityLogEntity> logs = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            stmt.setString(2, operation);
            rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToEntity(rs));
            }
            return logs;
        } catch (SQLException e) {
            handleSQLException("Error finding activity logs by operation", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return logs;
    }

    public boolean deleteById(Long logId) throws DatabaseException {
        String sql = "DELETE FROM activity_logs WHERE log_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, logId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting activity log", e);
        } finally {
            closeResources(stmt, conn);
        }
        return false;
    }

    public int deleteByRepoId(Long repoId) throws DatabaseException {
        String sql = "DELETE FROM activity_logs WHERE repo_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows;
        } catch (SQLException e) {
            handleSQLException("Error deleting activity logs by repository ID", e);
        } finally {
            closeResources(stmt, conn);
        }
        return 0;
    }

    /**
     * Get total count of all activity logs in the database
     */
    public int getActivityLogCount() throws DatabaseException {
        String sql = "SELECT COUNT(*) as total FROM activity_logs";
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
            handleSQLException("Error counting activity logs", e);
        } finally {
            closeResources(rs, stmt, conn);
        }
        return 0;
    }

    private ActivityLogEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        ActivityLogEntity entity = new ActivityLogEntity();
        entity.setLogId(rs.getLong("log_id"));
        entity.setRepoId(rs.getLong("repo_id"));
        entity.setOperation(rs.getString("operation"));
        entity.setDetails(rs.getString("details"));
        entity.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return entity;
    }
}
