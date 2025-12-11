package com.azaala.vcs.persistence;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Manager for Azaala VCS.
 * Handles database initialization, schema creation, and migrations.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private ConnectionPool connectionPool;
    private DatabaseConfig config;
    private static final String SCHEMA_FILE = "data/schema.sql";

    private DatabaseManager() {
        this.connectionPool = ConnectionPool.getInstance();
        this.config = DatabaseConfig.getInstance();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() throws DatabaseException {
        System.out.println("\n=== Initializing Database ===");

        connectionPool.initialize();

        if (!connectionPool.testConnection()) {
            throw new DatabaseException("Failed to connect to MySQL database. Check your database configuration.");
        }

        if (config.isAutoInitEnabled()) {
            initializeSchema();
        }

        System.out.println("✓ Database initialization complete\n");
    }

    public void initializeSchema() throws DatabaseException {
        try {
            String schemaSQL = readSchemaFile();
            if (schemaSQL == null || schemaSQL.isEmpty()) {
                System.out.println("⚠ Schema file is empty or not found");
                return;
            }

            String[] statements = schemaSQL.split(";");
            int count = 0;

            try (Connection conn = connectionPool.getConnection();
                 Statement stmt = conn.createStatement()) {

                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty() && !sql.startsWith("--")) {
                        try {
                            stmt.execute(sql);
                            count++;
                        } catch (Exception e) {
                            System.out.println("  (Skipped: " + sql.substring(0, Math.min(50, sql.length())) + "...)");
                        }
                    }
                }

                System.out.println("✓ Schema initialized successfully (" + count + " statements)");

            } catch (Exception e) {
                throw new DatabaseException("Error executing schema SQL: " + e.getMessage(), e);
            }

        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Error initializing schema: " + e.getMessage(), e);
        }
    }

    private String readSchemaFile() throws IOException {
        File schemaFile = new File(SCHEMA_FILE);

        if (!schemaFile.exists()) {
            System.out.println("⚠ Schema file not found: " + SCHEMA_FILE);
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(schemaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--")) {
                    content.append(line).append("\n");
                }
            }
        }

        return content.toString();
    }

    public boolean isInitialized() {
        return connectionPool.isInitialized() && connectionPool.testConnection();
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public void close() {
        if (connectionPool != null) {
            connectionPool.close();
        }
    }

    public String getConnectionInfo() {
        if (!isInitialized()) {
            return "Database not initialized";
        }

        return "Database: " + config.getMysqlUrl() + "\n" +
               "Active Connections: " + connectionPool.getPoolSize() + "\n" +
               "Idle Connections: " + connectionPool.getIdleConnections() + "\n" +
               "Max Pool Size: " + config.getMaximumPoolSize();
    }

    /**
     * Execute a query and return results
     */
    public List<List<Object>> executeQuery(String sql, Object... params) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<List<Object>> results = new ArrayList<>();

        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            rs = stmt.executeQuery();
            ResultSetMetaData metadata = rs.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (rs.next()) {
                List<Object> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(rs.getObject(i));
                }
                results.add(row);
            }

            return results;
        } catch (SQLException e) {
            throw new DatabaseException("Error executing query: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Execute an update/insert/delete and return affected rows
     */
    public int executeUpdate(String sql, Object... params) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error executing update: " + e.getMessage(), e);
        } finally {
            closeResources(stmt, conn);
        }
    }

    /**
     * Execute a transaction with automatic rollback on failure
     */
    public <T> T executeTransaction(TransactionCallback<T> callback) throws DatabaseException {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            conn.setAutoCommit(false);

            T result = callback.doInTransaction(conn);

            conn.commit();
            return result;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            throw new DatabaseException("Transaction failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Callback interface for transaction execution
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(Connection conn) throws SQLException, DatabaseException;
    }

    /**
     * Close resources safely
     */
    private void closeResources(AutoCloseable... resources) {
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
}

