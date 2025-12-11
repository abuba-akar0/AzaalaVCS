package com.azaala.vcs.gui;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * PreferencesManager - Centralized management of all application preferences
 * Handles loading, saving, and accessing all user preferences
 */
public class PreferencesManager {

    private static final String PREFERENCES_FILE = ".azaala_preferences";
    private static final String PREFS_DIR = System.getProperty("user.home") + File.separator + ".azaala";
    private static final String PREFS_PATH = PREFS_DIR + File.separator + PREFERENCES_FILE;

    private static PreferencesManager instance;
    private Properties preferences;

    // Preference keys
    public static final String THEME = "ui.theme";
    public static final String ENABLE_LOGGING = "app.enable.logging";
    public static final String AUTO_REFRESH = "app.auto.refresh";
    public static final String AUTO_REFRESH_INTERVAL = "app.auto.refresh.interval";
    public static final String AUTHOR_NAME = "commit.author.name";
    public static final String AUTHOR_EMAIL = "commit.author.email";
    public static final String DEFAULT_COMMIT_MSG = "commit.default.message";
    public static final String REQUIRE_COMMIT_MSG = "commit.require.message";
    public static final String MAX_FILE_SIZE = "file.max.size.mb";
    public static final String EXCLUDE_HIDDEN_FILES = "file.exclude.hidden";
    public static final String IGNORE_PATTERNS = "file.ignore.patterns";
    public static final String ENABLE_COMPRESSION = "advanced.compression.enabled";
    public static final String MAX_RECENT_REPOS = "advanced.max.recent.repos";
    public static final String SHOW_DETAILED_DIFF = "advanced.show.detailed.diff";

    private PreferencesManager() {
        this.preferences = new Properties();
        loadPreferences();
    }

    /**
     * Get singleton instance
     */
    public static synchronized PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager();
        }
        return instance;
    }

    /**
     * Load preferences from file
     */
    private void loadPreferences() {
        try {
            File prefsFile = new File(PREFS_PATH);
            if (prefsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(prefsFile)) {
                    preferences.load(fis);
                }
            } else {
                // Load defaults if file doesn't exist
                loadDefaults();
            }
        } catch (IOException e) {
            System.err.println("Error loading preferences: " + e.getMessage());
            loadDefaults();
        }
    }

    /**
     * Load default preferences
     */
    private void loadDefaults() {
        preferences.clear();

        // UI/General preferences
        setDefault(THEME, "System Default");
        setDefault(ENABLE_LOGGING, "false");
        setDefault(AUTO_REFRESH, "true");
        setDefault(AUTO_REFRESH_INTERVAL, "2");

        // Commit preferences
        setDefault(AUTHOR_NAME, "");
        setDefault(AUTHOR_EMAIL, "");
        setDefault(DEFAULT_COMMIT_MSG, "");
        setDefault(REQUIRE_COMMIT_MSG, "true");

        // File preferences
        setDefault(MAX_FILE_SIZE, "500");
        setDefault(EXCLUDE_HIDDEN_FILES, "true");
        setDefault(IGNORE_PATTERNS, "*.log\n.DS_Store\n/target\n/bin");

        // Advanced preferences
        setDefault(ENABLE_COMPRESSION, "false");
        setDefault(MAX_RECENT_REPOS, "10");
        setDefault(SHOW_DETAILED_DIFF, "true");
    }

    /**
     * Set default preference if not already set
     */
    private void setDefault(String key, String value) {
        if (!preferences.containsKey(key)) {
            preferences.setProperty(key, value);
        }
    }

    /**
     * Save all preferences to file
     */
    public boolean savePreferences() {
        try {
            // Create directory if it doesn't exist
            File dir = new File(PREFS_DIR);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    System.err.println("Failed to create preferences directory: " + PREFS_DIR);
                    return false;
                }
            }

            // Check write permission
            if (!dir.canWrite()) {
                System.err.println("Preferences directory is not writable: " + PREFS_DIR);
                return false;
            }

            // Save preferences
            try (FileOutputStream fos = new FileOutputStream(PREFS_PATH)) {
                preferences.store(fos, "Azaala VCS Preferences - " + new java.util.Date());
            }

            return true;

        } catch (IOException e) {
            System.err.println("Error saving preferences: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get string preference with default
     */
    public String getString(String key, String defaultValue) {
        return preferences.getProperty(key, defaultValue);
    }

    /**
     * Get boolean preference with default
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = preferences.getProperty(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    /**
     * Get integer preference with default
     */
    public int getInt(String key, int defaultValue) {
        try {
            String value = preferences.getProperty(key, String.valueOf(defaultValue));
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Set preference value
     */
    public void set(String key, String value) {
        preferences.setProperty(key, value);
    }

    /**
     * Set boolean preference
     */
    public void setBoolean(String key, boolean value) {
        preferences.setProperty(key, String.valueOf(value));
    }

    /**
     * Set integer preference
     */
    public void setInt(String key, int value) {
        preferences.setProperty(key, String.valueOf(value));
    }

    /**
     * Reset to defaults
     */
    public void resetToDefaults() {
        loadDefaults();
        savePreferences();
    }

    /**
     * Get all preferences as Properties
     */
    public Properties getAllPreferences() {
        return new Properties(preferences);
    }

    /**
     * Get preferences file path
     */
    public String getPreferencesPath() {
        return PREFS_PATH;
    }

    /**
     * Validate author information
     */
    public boolean validateAuthor(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return false;
        }
        return true;
    }

    /**
     * Validate file size
     */
    public boolean validateFileSize(int sizeMb) {
        return sizeMb > 0 && sizeMb <= 10000;
    }

    /**
     * Validate refresh interval
     */
    public boolean validateRefreshInterval(int seconds) {
        return seconds >= 1 && seconds <= 60;
    }

    /**
     * Get author full information
     */
    public String getAuthorInfo() {
        String name = getString(AUTHOR_NAME, "");
        String email = getString(AUTHOR_EMAIL, "");
        if (!name.isEmpty() && !email.isEmpty()) {
            return name + " <" + email + ">";
        }
        return "";
    }
}

