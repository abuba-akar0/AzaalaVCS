package com.azaala.vcs.persistence.dao;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.sql.*;
import com.azaala.vcs.persistence.models.RepositoryEntity;
import com.azaala.vcs.persistence.DatabaseException;

public class RepositoryDAO extends BaseDAO {

    public Long create(RepositoryEntity repository) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "INSERT INTO repositories (repo_name, repo_path, created_at, description) VALUES (?, ?, ?, ?)";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, repository.getRepoName());
            stmt.setString(2, repository.getRepoPath());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, repository.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new DatabaseException("Failed to create repository");

            rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getLong(1);
            else throw new DatabaseException("Failed to get generated repository ID");
        } catch (SQLException e) {
            handleSQLException("Error creating repository", e);
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public RepositoryEntity findById(Long repoId) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM repositories WHERE repo_id = ?";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);
            rs = stmt.executeQuery();

            if (rs.next()) return mapResultSetToEntity(rs);
            return null;
        } catch (SQLException e) {
            handleSQLException("Error finding repository by ID", e);
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public RepositoryEntity findByPath(String repoPath) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM repositories WHERE repo_path = ?";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, repoPath);
            rs = stmt.executeQuery();

            if (rs.next()) return mapResultSetToEntity(rs);
            return null;
        } catch (SQLException e) {
            handleSQLException("Error finding repository by path", e);
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public List<RepositoryEntity> findAll() throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<RepositoryEntity> repositories = new ArrayList<>();
        String sql = "SELECT * FROM repositories ORDER BY created_at DESC";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) repositories.add(mapResultSetToEntity(rs));
            return repositories;
        } catch (SQLException e) {
            handleSQLException("Error retrieving all repositories", e);
            return repositories;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public boolean update(RepositoryEntity repository) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "UPDATE repositories SET repo_name = ?, repo_path = ?, description = ?, last_commit_at = ? WHERE repo_id = ?";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, repository.getRepoName());
            stmt.setString(2, repository.getRepoPath());
            stmt.setString(3, repository.getDescription());
            stmt.setTimestamp(4, repository.getLastCommitAt() != null ? Timestamp.valueOf(repository.getLastCommitAt()) : null);
            stmt.setLong(5, repository.getRepoId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error updating repository", e);
            return false;
        } finally {
            closeResources(stmt, conn);
        }
    }

    public boolean deleteById(Long repoId) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "DELETE FROM repositories WHERE repo_id = ?";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, repoId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException("Error deleting repository", e);
            return false;
        } finally {
            closeResources(stmt, conn);
        }
    }

    private RepositoryEntity mapResultSetToEntity(ResultSet rs) throws SQLException {
        RepositoryEntity entity = new RepositoryEntity();
        entity.setRepoId(rs.getLong("repo_id"));
        entity.setRepoName(rs.getString("repo_name"));
        entity.setRepoPath(rs.getString("repo_path"));
        entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp lastCommitTs = rs.getTimestamp("last_commit_at");
        if (lastCommitTs != null) entity.setLastCommitAt(lastCommitTs.toLocalDateTime());
        entity.setDescription(rs.getString("description"));
        return entity;
    }
}