package com.azaala.vcs.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * HikariCP-based connection pool manager for MySQL database.
 * Provides thread-safe connection pooling for the VCS application.
 */
public class ConnectionPool {
    private static ConnectionPool instance;
    private HikariDataSource dataSource;
    private boolean initialized = false;

    private ConnectionPool() {
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    public synchronized void initialize() throws DatabaseException {
        if (initialized) {
            System.out.println("✓ Connection pool already initialized");
            return;
        }

        try {
            DatabaseConfig config = DatabaseConfig.getInstance();

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getMysqlUrl());
            hikariConfig.setUsername(config.getMysqlUsername());
            hikariConfig.setPassword(config.getMysqlPassword());
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setMinimumIdle(config.getMinimumIdleConnections());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(config.getIdleTimeout());
            hikariConfig.setMaxLifetime(config.getMaxLifetime());
            hikariConfig.setPoolName("AzaalaVCS-Pool");
            hikariConfig.setAutoCommit(true);

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new DatabaseException("MySQL JDBC driver not found. Please add mysql-connector-java to classpath.", e);
            }

            dataSource = new HikariDataSource(hikariConfig);
            initialized = true;
            System.out.println("✓ Connection pool initialized successfully");
            System.out.println("  URL: " + config.getMysqlUrl());
            System.out.println("  Pool Size: " + config.getMaximumPoolSize());

        } catch (Exception e) {
            throw new DatabaseException("Failed to initialize connection pool: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws DatabaseException {
        if (!initialized) {
            throw new DatabaseException("Connection pool not initialized. Call initialize() first.");
        }

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get connection from pool: " + e.getMessage(), e);
        }
    }

    public synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            initialized = false;
            System.out.println("✓ Connection pool closed successfully");
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getPoolSize() {
        if (dataSource == null) {
            return 0;
        }
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    public int getIdleConnections() {
        if (dataSource == null) {
            return 0;
        }
        return dataSource.getHikariPoolMXBean().getIdleConnections();
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return !conn.isClosed();
        } catch (Exception e) {
            System.err.println("✗ Connection test failed: " + e.getMessage());
            return false;
        }
    }
}

