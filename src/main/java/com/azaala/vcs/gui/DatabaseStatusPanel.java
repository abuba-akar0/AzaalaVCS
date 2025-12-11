package com.azaala.vcs.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.azaala.vcs.persistence.DatabaseManager;
import com.azaala.vcs.persistence.DatabaseException;
import com.azaala.vcs.persistence.ConnectionPool;
import com.azaala.vcs.persistence.dao.*;
import com.azaala.vcs.persistence.models.*;

/**
 * DatabaseStatusPanel - Displays comprehensive database connection status and stored data
 * Shows connection pool stats, database configuration, and data statistics
 */
public class DatabaseStatusPanel extends JPanel {
    private DatabaseManager dbManager;
    private JLabel statusLabel;
    private JLabel connectionInfoLabel;
    private JLabel poolStatsLabel;
    private JTextArea databaseInfoArea;
    private JButton refreshButton;
    private Timer refreshTimer;

    public DatabaseStatusPanel() {
        this.dbManager = DatabaseManager.getInstance();
        initializePanel();
        setupAutoRefresh();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                 UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));

        // Top panel: Status and controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center panel: Database information
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel: Data statistics
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        refreshDatabaseStatus();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            "Database Connection Status",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            UITheme.LABEL_FONT,
            UITheme.TEXT_PRIMARY
        ));

        // Status label
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(UITheme.SUBTITLE_FONT);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(statusLabel, BorderLayout.WEST);

        // Connection info
        connectionInfoLabel = new JLabel("Loading...");
        connectionInfoLabel.setFont(UITheme.CONTENT_FONT);
        panel.add(connectionInfoLabel, BorderLayout.CENTER);

        // Refresh button
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(UITheme.LABEL_FONT);
        refreshButton.setPreferredSize(new Dimension(100, 30));
        refreshButton.addActionListener(e -> refreshDatabaseStatus());
        UITheme.stylePrimaryButton(refreshButton);
        panel.add(refreshButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            "Database Information & Statistics",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            UITheme.LABEL_FONT,
            UITheme.TEXT_PRIMARY
        ));

        // Scrollable text area for database info
        databaseInfoArea = new JTextArea();
        databaseInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        databaseInfoArea.setEditable(false);
        databaseInfoArea.setBackground(new Color(240, 240, 240));
        databaseInfoArea.setForeground(UITheme.TEXT_PRIMARY);
        databaseInfoArea.setMargin(new Insets(10, 10, 10, 10));
        databaseInfoArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(databaseInfoArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
            "Connection Pool Statistics",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            UITheme.LABEL_FONT,
            UITheme.TEXT_PRIMARY
        ));

        poolStatsLabel = new JLabel("Loading pool statistics...");
        poolStatsLabel.setFont(UITheme.CONTENT_FONT);
        UITheme.styleLabel(poolStatsLabel);
        panel.add(poolStatsLabel, BorderLayout.CENTER);

        return panel;
    }

    private void refreshDatabaseStatus() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return generateDatabaseStatusInfo();
            }

            @Override
            protected void done() {
                try {
                    String info = get();
                    databaseInfoArea.setText(info);
                    databaseInfoArea.setCaretPosition(0);
                } catch (Exception e) {
                    databaseInfoArea.setText("Error loading database information:\n" + e.getMessage());
                }
            }
        };
        worker.execute();

        // Update status label
        updateStatusLabel();

        // Update pool stats
        updatePoolStats();
    }

    private void updateStatusLabel() {
        try {
            if (dbManager.isInitialized()) {
                statusLabel.setText("✓ CONNECTED");
                statusLabel.setBackground(new Color(34, 139, 34)); // Green
                statusLabel.setForeground(Color.WHITE);
                connectionInfoLabel.setText("Database: " + dbManager.getConfig().getMysqlUrl());
                connectionInfoLabel.setForeground(new Color(34, 139, 34));
            } else {
                statusLabel.setText("✗ DISCONNECTED");
                statusLabel.setBackground(new Color(178, 34, 34)); // Red
                statusLabel.setForeground(Color.WHITE);
                connectionInfoLabel.setText("Database not connected");
                connectionInfoLabel.setForeground(new Color(178, 34, 34));
            }
        } catch (Exception e) {
            statusLabel.setText("⚠ ERROR");
            statusLabel.setBackground(new Color(255, 140, 0)); // Orange
            statusLabel.setForeground(Color.WHITE);
            connectionInfoLabel.setText("Error: " + e.getMessage());
        }
    }

    private void updatePoolStats() {
        try {
            if (dbManager.isInitialized()) {
                ConnectionPool pool = dbManager.getConnectionPool();
                int active = pool.getPoolSize();
                int idle = pool.getIdleConnections();
                int max = dbManager.getConfig().getMaximumPoolSize();

                String stats = String.format(
                    "Active Connections: %d/%d | Idle: %d | Max Pool Size: %d",
                    active, (active + idle), idle, max
                );
                poolStatsLabel.setText(stats);
                poolStatsLabel.setForeground(new Color(34, 139, 34));
            }
        } catch (Exception e) {
            poolStatsLabel.setText("Unable to retrieve pool statistics");
            poolStatsLabel.setForeground(new Color(178, 34, 34));
        }
    }

    private String generateDatabaseStatusInfo() {
        StringBuilder info = new StringBuilder();

        try {
            // Database Configuration
            info.append("═══════════════════════════════════════════════════════════════\n");
            info.append("DATABASE CONFIGURATION\n");
            info.append("═══════════════════════════════════════════════════════════════\n");
            if (dbManager.isInitialized()) {
                info.append("Status:                  ✓ Connected\n");
                info.append("Database URL:           ").append(dbManager.getConfig().getMysqlUrl()).append("\n");
                info.append("Username:               ").append(dbManager.getConfig().getMysqlUsername()).append("\n");
                info.append("Max Pool Size:          ").append(dbManager.getConfig().getMaximumPoolSize()).append("\n");
                info.append("Min Idle Connections:   ").append(dbManager.getConfig().getMinimumIdleConnections()).append("\n");
                info.append("Connection Timeout:     ").append(dbManager.getConfig().getConnectionTimeout()).append(" ms\n");
                info.append("Idle Timeout:           ").append(dbManager.getConfig().getIdleTimeout()).append(" ms\n");
                info.append("Max Lifetime:           ").append(dbManager.getConfig().getMaxLifetime()).append(" ms\n");
            } else {
                info.append("Status:                  ✗ Not Connected\n");
            }

            // Database Statistics
            info.append("\n");
            info.append("═══════════════════════════════════════════════════════════════\n");
            info.append("DATABASE STATISTICS\n");
            info.append("═══════════════════════════════════════════════════════════════\n");

            DatabaseStatistics stats = getDatabaseStatistics();
            info.append("Repositories:           ").append(stats.repositoryCount).append("\n");
            info.append("Commits:                ").append(stats.commitCount).append("\n");
            info.append("Commit Files:           ").append(stats.commitFileCount).append("\n");
            info.append("Staged Files:           ").append(stats.stagedFileCount).append("\n");
            info.append("Activity Logs:          ").append(stats.activityLogCount).append("\n");

            // Data Storage Summary
            info.append("\n");
            info.append("═══════════════════════════════════════════════════════════════\n");
            info.append("DATA STORAGE SUMMARY\n");
            info.append("═══════════════════════════════════════════════════════════════\n");

            if (stats.repositoryCount > 0) {
                info.append("\n📦 REPOSITORIES\n");
                info.append("─────────────────────────────────────────────────────────────\n");
                for (String repo : stats.repositoryNames) {
                    info.append("  • ").append(repo).append("\n");
                }
            }

            if (stats.recentCommits.size() > 0) {
                info.append("\n📝 RECENT COMMITS (Last 10)\n");
                info.append("─────────────────────────────────────────────────────────────\n");
                for (String commit : stats.recentCommits) {
                    info.append("  ").append(commit).append("\n");
                }
            }

            if (stats.stagedFileCount > 0) {
                info.append("\n📌 STAGED FILES\n");
                info.append("─────────────────────────────────────────────────────────────\n");
                info.append("  Total files staged: ").append(stats.stagedFileCount).append("\n");
            }

            // Connection Info
            info.append("\n");
            info.append("═══════════════════════════════════════════════════════════════\n");
            info.append("CONNECTION POOL INFO\n");
            info.append("═══════════════════════════════════════════════════════════════\n");
            if (dbManager.isInitialized()) {
                ConnectionPool pool = dbManager.getConnectionPool();
                info.append("Pool Name:              AzaalaVCS-Pool\n");
                info.append("Active Connections:    ").append(pool.getPoolSize()).append("\n");
                info.append("Idle Connections:      ").append(pool.getIdleConnections()).append("\n");
                info.append("Total Available:       ").append(pool.getPoolSize() + pool.getIdleConnections()).append("\n");
                info.append("Max Capacity:          ").append(dbManager.getConfig().getMaximumPoolSize()).append("\n");
            }

        } catch (Exception e) {
            info.append("Error retrieving database information:\n");
            info.append(e.getMessage()).append("\n");
            e.printStackTrace();
        }

        return info.toString();
    }

    private DatabaseStatistics getDatabaseStatistics() {
        DatabaseStatistics stats = new DatabaseStatistics();

        try {
            // Get repository count
            RepositoryDAO repoDAO = new RepositoryDAO();
            List<RepositoryEntity> repos = repoDAO.findAll();
            stats.repositoryCount = repos != null ? repos.size() : 0;
            if (repos != null) {
                for (RepositoryEntity repo : repos) {
                    stats.repositoryNames.add(repo.getRepoName() + " (" + repo.getRepoPath() + ")");
                }
            }

            // Get commit count and recent commits
            CommitDAO commitDAO = new CommitDAO();
            stats.commitCount = commitDAO.getCommitCount();
            List<CommitEntity> recentCommits = commitDAO.findRecent(10);
            if (recentCommits != null) {
                for (CommitEntity commit : recentCommits) {
                    stats.recentCommits.add(
                        commit.getCommitId() + " - " + commit.getMessage().substring(0, Math.min(40, commit.getMessage().length())) +
                        " (" + commit.getTimestamp() + ")"
                    );
                }
            }

            // Get commit file count
            CommitFileDAO commitFileDAO = new CommitFileDAO();
            stats.commitFileCount = commitFileDAO.getCommitFileCount();

            // Get staged file count
            StagedFileDAO stagedFileDAO = new StagedFileDAO();
            List<StagedFileEntity> stagedFiles = stagedFileDAO.findAll();
            stats.stagedFileCount = stagedFiles != null ? stagedFiles.size() : 0;

            // Get activity log count
            ActivityLogDAO activityLogDAO = new ActivityLogDAO();
            stats.activityLogCount = activityLogDAO.getActivityLogCount();

        } catch (Exception e) {
            System.err.println("Error retrieving database statistics: " + e.getMessage());
        }

        return stats;
    }

    private void setupAutoRefresh() {
        // Refresh every 5 seconds
        refreshTimer = new Timer(5000, e -> refreshDatabaseStatus());
        refreshTimer.start();
    }

    public void cleanup() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Inner class to hold database statistics
     */
    private static class DatabaseStatistics {
        int repositoryCount = 0;
        int commitCount = 0;
        int commitFileCount = 0;
        int stagedFileCount = 0;
        int activityLogCount = 0;
        List<String> repositoryNames = new ArrayList<>();
        List<String> recentCommits = new ArrayList<>();
    }
}

