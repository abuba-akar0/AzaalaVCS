package com.azaala.vcs.persistence;

import java.io.*;
import java.util.Properties;

/**
 * Configuration manager for database connection properties.
 * Loads configuration from db.properties file and provides access to settings.
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private Properties properties;
    private static final String CONFIG_FILE = "db.properties";

    private DatabaseConfig() {
        properties = new Properties();
        loadConfiguration();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadConfiguration() {
        InputStream input = null;
        try {
            // First, try to load from classpath (works for JAR files and compiled classes)
            input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE);

            if (input != null) {
                properties.load(input);
                System.out.println("✓ Database configuration loaded successfully from classpath");
            } else {
                // Fallback: try to load from file system for development
                try {
                    input = new FileInputStream("src/main/resources/" + CONFIG_FILE);
                    properties.load(input);
                    System.out.println("✓ Database configuration loaded successfully from file system");
                } catch (FileNotFoundException e) {
                    System.err.println("⚠ Configuration file not found: " + CONFIG_FILE);
                    System.err.println("  Using default values");
                    loadDefaults();
                }
            }
        } catch (IOException e) {
            System.err.println("✗ Error loading configuration: " + e.getMessage());
            loadDefaults();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private void loadDefaults() {
        properties.setProperty("mysql.url", "jdbc:mysql://localhost:3306/azaala_vcs");
        properties.setProperty("mysql.username", "root");
        properties.setProperty("mysql.password", "");
        properties.setProperty("hikari.maximumPoolSize", "10");
        properties.setProperty("hikari.minimumIdleConnections", "2");
        properties.setProperty("hikari.connectionTimeout", "30000");
        properties.setProperty("hikari.idleTimeout", "600000");
        properties.setProperty("hikari.maxLifetime", "1800000");
        properties.setProperty("hybrid.storage.enabled", "true");
        properties.setProperty("hybrid.storage.content.database", "false");
        properties.setProperty("audit.logging.enabled", "true");
        properties.setProperty("database.auto.init", "true");
        properties.setProperty("logging.level", "INFO");
        properties.setProperty("logging.queries.enabled", "false");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getMysqlUrl() {
        return getProperty("mysql.url", "jdbc:mysql://localhost:3306/azaala_vcs");
    }

    public String getMysqlUsername() {
        return getProperty("mysql.username", "root");
    }

    public String getMysqlPassword() {
        return getProperty("mysql.password", "");
    }

    public int getMaximumPoolSize() {
        return getIntProperty("hikari.maximumPoolSize", 10);
    }

    public int getMinimumIdleConnections() {
        return getIntProperty("hikari.minimumIdleConnections", 2);
    }

    public long getConnectionTimeout() {
        return getIntProperty("hikari.connectionTimeout", 30000);
    }

    public long getIdleTimeout() {
        return getIntProperty("hikari.idleTimeout", 600000);
    }

    public long getMaxLifetime() {
        return getIntProperty("hikari.maxLifetime", 1800000);
    }

    public boolean isHybridStorageEnabled() {
        return getBooleanProperty("hybrid.storage.enabled", true);
    }

    public boolean isContentStoredInDatabase() {
        return getBooleanProperty("hybrid.storage.content.database", false);
    }

    public boolean isAuditLoggingEnabled() {
        return getBooleanProperty("audit.logging.enabled", true);
    }

    public boolean isAutoInitEnabled() {
        return getBooleanProperty("database.auto.init", true);
    }

    public String getLoggingLevel() {
        return getProperty("logging.level", "INFO");
    }

    public boolean isQueryLoggingEnabled() {
        return getBooleanProperty("logging.queries.enabled", false);
    }
}

