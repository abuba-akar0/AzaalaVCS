package com.azaala.vcs.persistence.dao;

import com.azaala.vcs.persistence.ConnectionPool;
import com.azaala.vcs.persistence.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstract base class for Data Access Objects (DAO).
 * Provides common CRUD operations and connection management.
 */
public abstract class BaseDAO {
    protected ConnectionPool connectionPool;

    public BaseDAO() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    protected Connection getConnection() throws DatabaseException {
        return connectionPool.getConnection();
    }

    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    protected void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error closing statement: " + e.getMessage());
            }
        }
    }

    protected void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Error closing result set: " + e.getMessage());
            }
        }
    }

    protected void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    protected void logOperation(String operation, String details) {
        // Can be extended for audit logging
    }

    protected void handleSQLException(String message, SQLException e) throws DatabaseException {
        String errorMsg = message + " - SQL Error: " + e.getMessage() + " (Code: " + e.getErrorCode() + ")";
        throw new DatabaseException(errorMsg, e);
    }
}

