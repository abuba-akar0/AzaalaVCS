package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;

/**
 * Settings Panel - Displays and manages configuration options
 */
public class SettingsPanel extends JPanel {

    private VCS vcs;
    private Repository repository;
    private JTextField txtDefaultCommitMsg;
    private JTextField txtMaxFileSize;
    private JCheckBox chkAutoRefresh;
    private JLabel statusLabel;

    private static final String SETTINGS_FILE = "settings.properties";
    private Properties settings;

    public SettingsPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;
        this.settings = new Properties();

        setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                                  UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));

        // Add info panel
        JPanel infoPanel = UITheme.createInfoPanel(
            "Settings",
            "Configure repository and application preferences. " +
            "Customize commit messages, file size limits, and auto-refresh behavior. " +
            "Changes here are automatically saved."
        );
        add(infoPanel, BorderLayout.NORTH);

        // Create settings panel
        JPanel settingsPanel = createSettingsPanel();
        JScrollPane scrollPane = new JScrollPane(settingsPanel);
        scrollPane.setBorder(UITheme.createStyledBorder("Configuration"));
        add(scrollPane, BorderLayout.CENTER);

        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout(UITheme.SPACING_COMPONENT, UITheme.SPACING_COMPONENT));
        statusPanel.setBackground(UITheme.PANEL_BACKGROUND);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM,
                                                              UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));

        statusLabel = new JLabel("Settings ready");
        UITheme.styleLabel(statusLabel);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        JButton btnSave = new JButton("💾 Save Settings");
        btnSave.addActionListener(e -> saveSettings());
        UITheme.styleSuccessButton(btnSave);
        statusPanel.add(btnSave, BorderLayout.EAST);

        add(statusPanel, BorderLayout.SOUTH);

        // Load existing settings
        loadSettings();
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, UITheme.SPACING_SECTION, UITheme.SPACING_SECTION));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE,
                                                        UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));

        // Default Commit Message
        JLabel lbl1 = new JLabel("Default Commit Message:");
        UITheme.styleLabel(lbl1);
        panel.add(lbl1);

        txtDefaultCommitMsg = new JTextField();
        UITheme.styleTextField(txtDefaultCommitMsg);
        panel.add(txtDefaultCommitMsg);

        // Max File Size
        JLabel lbl2 = new JLabel("Max File Size (MB):");
        UITheme.styleLabel(lbl2);
        panel.add(lbl2);

        txtMaxFileSize = new JTextField("100");
        UITheme.styleTextField(txtMaxFileSize);
        panel.add(txtMaxFileSize);

        // Auto-refresh
        JLabel lbl3 = new JLabel("Auto-refresh on tab change:");
        UITheme.styleLabel(lbl3);
        panel.add(lbl3);

        chkAutoRefresh = new JCheckBox();
        chkAutoRefresh.setSelected(true);
        chkAutoRefresh.setBackground(UITheme.BACKGROUND_COLOR);
        panel.add(chkAutoRefresh);

        // Spacers
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        return panel;
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    private void saveSettings() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Update settings
            settings.setProperty("default.commit.message", txtDefaultCommitMsg.getText());
            settings.setProperty("max.file.size", txtMaxFileSize.getText());
            settings.setProperty("auto.refresh", String.valueOf(chkAutoRefresh.isSelected()));

            // Determine settings path
            String repoPath = repository != null ? repository.getPath() : System.getProperty("user.home");
            Path settingsPath = Paths.get(repoPath, SETTINGS_FILE);
            File settingsFile = settingsPath.toFile();
            File parentDir = settingsFile.getParentFile();

            // Ensure parent directory exists and is writable
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    statusLabel.setText("✗ Cannot create settings directory");
                    statusLabel.setForeground(UITheme.ERROR_COLOR);
                    return;
                }
            }

            // Check write permission
            if (!parentDir.canWrite()) {
                statusLabel.setText("✗ Settings directory is not writable");
                statusLabel.setForeground(UITheme.ERROR_COLOR);
                return;
            }

            // Save to file
            try (FileWriter writer = new FileWriter(settingsFile)) {
                settings.store(writer, "Azaala VCS Settings");
            }

            statusLabel.setText("✓ Settings saved successfully at: " + settingsPath);
            statusLabel.setForeground(UITheme.SUCCESS_COLOR);

        } catch (IOException e) {
            statusLabel.setText("✗ Error saving settings: " + e.getMessage());
            statusLabel.setForeground(UITheme.ERROR_COLOR);
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        // Validate max file size
        try {
            int fileSize = Integer.parseInt(txtMaxFileSize.getText());
            if (fileSize <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Max file size must be a positive number",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Max file size must be a valid number",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    public void refresh() {
        loadSettings();
        statusLabel.setText("Settings ready");
        statusLabel.setForeground(UITheme.TEXT_PRIMARY);
    }

    private void loadSettings() {
        try {
            String repoPath = repository != null ? repository.getPath() : System.getProperty("user.home");
            Path settingsPath = Paths.get(repoPath, SETTINGS_FILE);

            if (Files.exists(settingsPath)) {
                try (FileReader reader = new FileReader(settingsPath.toFile())) {
                    settings.load(reader);
                    System.out.println("Settings loaded from: " + settingsPath);
                }
            } else {
                // Settings file doesn't exist yet - use defaults
                System.out.println("Settings file not found at: " + settingsPath + " (using defaults)");
            }

            // Apply loaded settings to UI (with defaults if not found)
            txtDefaultCommitMsg.setText(settings.getProperty("default.commit.message", ""));
            txtMaxFileSize.setText(settings.getProperty("max.file.size", "100"));
            chkAutoRefresh.setSelected(Boolean.parseBoolean(settings.getProperty("auto.refresh", "true")));

        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            // Continue with default values - don't crash
            txtMaxFileSize.setText("100");
            chkAutoRefresh.setSelected(true);
        }
    }
}
