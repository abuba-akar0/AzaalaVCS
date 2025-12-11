package com.azaala.vcs.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Professional Preferences Dialog for Azaala VCS
 * Provides comprehensive application settings management using PreferencesManager
 */
public class PreferencesDialog extends JDialog {

    private PreferencesManager prefManager;
    private JTabbedPane tabbedPane;

    // General settings
    private JCheckBox enableLoggingCheckBox;
    private JCheckBox autoRefreshCheckBox;
    private JSpinner autoRefreshIntervalSpinner;

    // Commit settings
    private JTextField authorNameField;
    private JTextField authorEmailField;
    private JTextField defaultCommitMsgField;
    private JCheckBox requireCommitMsgCheckBox;

    // File settings
    private JSpinner maxFileSizeSpinner;
    private JCheckBox excludeHiddenFilesCheckBox;
    private JTextArea ignorePatternArea;

    // Advanced settings
    private JCheckBox enableCompressionCheckBox;
    private JSpinner maxRecentReposSpinner;
    private JCheckBox showDetailedDiffCheckBox;

    private JLabel statusLabel;

    public PreferencesDialog(JFrame parent) {
        super(parent, "Preferences", true);
        this.prefManager = PreferencesManager.getInstance();

        initializeUI();
        loadPreferencesFromManager();
        setVisible(true);
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(getParent());
        setResizable(true);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel headerLabel = new JLabel("Application Preferences");
        headerLabel.setFont(UITheme.TITLE_FONT);
        headerLabel.setForeground(UITheme.PRIMARY_COLOR);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UITheme.BACKGROUND_COLOR);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);

        tabbedPane.addTab("📋 General", createGeneralTab());
        tabbedPane.addTab("📝 Commit", createCommitTab());
        tabbedPane.addTab("📁 Files", createFilesTab());
        tabbedPane.addTab("⚙️ Advanced", createAdvancedTab());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer with buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footerPanel.setBackground(UITheme.BACKGROUND_COLOR);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UITheme.CONTENT_FONT);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        footerPanel.add(statusLabel);

        JButton resetBtn = new JButton("↺ Reset to Defaults");
        resetBtn.setPreferredSize(new Dimension(140, 35));
        resetBtn.addActionListener(e -> resetToDefaults());
        UITheme.stylePrimaryButton(resetBtn);
        footerPanel.add(resetBtn);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> dispose());
        UITheme.stylePrimaryButton(cancelBtn);
        footerPanel.add(cancelBtn);

        JButton applyBtn = new JButton("✓ Apply");
        applyBtn.setPreferredSize(new Dimension(100, 35));
        applyBtn.addActionListener(e -> applySettings());
        UITheme.styleSuccessButton(applyBtn);
        footerPanel.add(applyBtn);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createGeneralTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createSectionLabel("Behavior"));
        enableLoggingCheckBox = new JCheckBox("Enable detailed logging");
        enableLoggingCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(enableLoggingCheckBox);
        panel.add(createFormRow("Logging:", enableLoggingCheckBox));

        autoRefreshCheckBox = new JCheckBox("Auto-refresh panels");
        autoRefreshCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(autoRefreshCheckBox);
        autoRefreshCheckBox.addActionListener(e -> autoRefreshIntervalSpinner.setEnabled(autoRefreshCheckBox.isSelected()));
        panel.add(createFormRow("Auto Refresh:", autoRefreshCheckBox));

        autoRefreshIntervalSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 60, 1));
        panel.add(createFormRow("Refresh Interval (sec):", autoRefreshIntervalSpinner));

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createCommitTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createSectionLabel("Author Information"));
        authorNameField = new JTextField();
        UITheme.styleTextField(authorNameField);
        panel.add(createFormRow("Author Name:", authorNameField));
        panel.add(Box.createVerticalStrut(8));

        authorEmailField = new JTextField();
        UITheme.styleTextField(authorEmailField);
        panel.add(createFormRow("Author Email:", authorEmailField));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createSectionLabel("Commit Behavior"));
        defaultCommitMsgField = new JTextField();
        UITheme.styleTextField(defaultCommitMsgField);
        panel.add(createFormRow("Default Message:", defaultCommitMsgField));
        panel.add(Box.createVerticalStrut(8));

        requireCommitMsgCheckBox = new JCheckBox("Require commit message");
        requireCommitMsgCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(requireCommitMsgCheckBox);
        panel.add(createFormRow("Validation:", requireCommitMsgCheckBox));

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createFilesTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createSectionLabel("File Handling"));
        maxFileSizeSpinner = new JSpinner(new SpinnerNumberModel(500, 1, 10000, 50));
        panel.add(createFormRow("Max File Size (MB):", maxFileSizeSpinner));
        panel.add(Box.createVerticalStrut(8));

        excludeHiddenFilesCheckBox = new JCheckBox("Exclude hidden files");
        excludeHiddenFilesCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(excludeHiddenFilesCheckBox);
        panel.add(createFormRow("Options:", excludeHiddenFilesCheckBox));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createSectionLabel("Ignore Patterns"));
        JLabel helpLabel = new JLabel("(One pattern per line, e.g., *.log, /temp, .DS_Store)");
        helpLabel.setFont(UITheme.CONTENT_FONT);
        helpLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(helpLabel);

        ignorePatternArea = new JTextArea(6, 40);
        ignorePatternArea.setFont(UITheme.MONOSPACE_FONT);
        ignorePatternArea.setLineWrap(true);
        ignorePatternArea.setWrapStyleWord(true);
        UITheme.styleTextArea(ignorePatternArea);
        JScrollPane scrollPane = new JScrollPane(ignorePatternArea);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        panel.add(scrollPane);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createAdvancedTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createSectionLabel("Performance"));
        enableCompressionCheckBox = new JCheckBox("Enable compression for commits");
        enableCompressionCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(enableCompressionCheckBox);
        panel.add(createFormRow("Compression:", enableCompressionCheckBox));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createSectionLabel("History"));
        maxRecentReposSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
        panel.add(createFormRow("Max Recent Repos:", maxRecentReposSpinner));
        panel.add(Box.createVerticalStrut(15));

        panel.add(createSectionLabel("Display"));
        showDetailedDiffCheckBox = new JCheckBox("Show detailed diff by default");
        showDetailedDiffCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(showDetailedDiffCheckBox);
        panel.add(createFormRow("Diff Display:", showDetailedDiffCheckBox));

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.SUBTITLE_FONT);
        label.setForeground(UITheme.PRIMARY_COLOR);
        label.setBorder(new EmptyBorder(10, 0, 8, 0));
        return label;
    }

    private JPanel createFormRow(String label, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(UITheme.BACKGROUND_COLOR);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(UITheme.LABEL_FONT);
        labelComp.setPreferredSize(new Dimension(150, 35));
        row.add(labelComp, BorderLayout.WEST);

        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.add(component, BorderLayout.CENTER);

        return row;
    }

    private void loadPreferencesFromManager() {
        enableLoggingCheckBox.setSelected(prefManager.getBoolean(PreferencesManager.ENABLE_LOGGING, false));
        autoRefreshCheckBox.setSelected(prefManager.getBoolean(PreferencesManager.AUTO_REFRESH, true));
        autoRefreshIntervalSpinner.setValue(prefManager.getInt(PreferencesManager.AUTO_REFRESH_INTERVAL, 2));

        authorNameField.setText(prefManager.getString(PreferencesManager.AUTHOR_NAME, ""));
        authorEmailField.setText(prefManager.getString(PreferencesManager.AUTHOR_EMAIL, ""));
        defaultCommitMsgField.setText(prefManager.getString(PreferencesManager.DEFAULT_COMMIT_MSG, ""));
        requireCommitMsgCheckBox.setSelected(prefManager.getBoolean(PreferencesManager.REQUIRE_COMMIT_MSG, true));

        maxFileSizeSpinner.setValue(prefManager.getInt(PreferencesManager.MAX_FILE_SIZE, 500));
        excludeHiddenFilesCheckBox.setSelected(prefManager.getBoolean(PreferencesManager.EXCLUDE_HIDDEN_FILES, true));
        ignorePatternArea.setText(prefManager.getString(PreferencesManager.IGNORE_PATTERNS, "*.log\n.DS_Store\n/target\n/bin"));

        enableCompressionCheckBox.setSelected(prefManager.getBoolean(PreferencesManager.ENABLE_COMPRESSION, false));
        maxRecentReposSpinner.setValue(prefManager.getInt(PreferencesManager.MAX_RECENT_REPOS, 10));
        showDetailedDiffCheckBox.setSelected(prefManager.getBoolean(PreferencesManager.SHOW_DETAILED_DIFF, true));
    }

    private void applySettings() {
        try {
            if (!validateInputs()) {
                return;
            }

            // Save all settings to PreferencesManager (no theme)
            prefManager.setBoolean(PreferencesManager.ENABLE_LOGGING, enableLoggingCheckBox.isSelected());
            prefManager.setBoolean(PreferencesManager.AUTO_REFRESH, autoRefreshCheckBox.isSelected());
            prefManager.setInt(PreferencesManager.AUTO_REFRESH_INTERVAL, (Integer) autoRefreshIntervalSpinner.getValue());

            prefManager.set(PreferencesManager.AUTHOR_NAME, authorNameField.getText().trim());
            prefManager.set(PreferencesManager.AUTHOR_EMAIL, authorEmailField.getText().trim());
            prefManager.set(PreferencesManager.DEFAULT_COMMIT_MSG, defaultCommitMsgField.getText());
            prefManager.setBoolean(PreferencesManager.REQUIRE_COMMIT_MSG, requireCommitMsgCheckBox.isSelected());

            prefManager.setInt(PreferencesManager.MAX_FILE_SIZE, (Integer) maxFileSizeSpinner.getValue());
            prefManager.setBoolean(PreferencesManager.EXCLUDE_HIDDEN_FILES, excludeHiddenFilesCheckBox.isSelected());
            prefManager.set(PreferencesManager.IGNORE_PATTERNS, ignorePatternArea.getText());

            prefManager.setBoolean(PreferencesManager.ENABLE_COMPRESSION, enableCompressionCheckBox.isSelected());
            prefManager.setInt(PreferencesManager.MAX_RECENT_REPOS, (Integer) maxRecentReposSpinner.getValue());
            prefManager.setBoolean(PreferencesManager.SHOW_DETAILED_DIFF, showDetailedDiffCheckBox.isSelected());

            // Save to file
            if (prefManager.savePreferences()) {
                System.out.println("✓ Preferences saved to: " + prefManager.getPreferencesPath());

                statusLabel.setText("✓ Settings saved successfully!");
                statusLabel.setForeground(UITheme.SUCCESS_COLOR);

                JOptionPane.showMessageDialog(this,
                    "✓ Preferences saved successfully!\n\n" +
                    "Location: " + prefManager.getPreferencesPath() + "\n\n" +
                    "Note: Some settings will apply on next application restart.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                dispose();
            } else {
                statusLabel.setText("✗ Failed to save settings");
                statusLabel.setForeground(UITheme.ERROR_COLOR);

                JOptionPane.showMessageDialog(this,
                    "Failed to save preferences.\n\nPlease check file permissions.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("✗ Error saving settings");
            statusLabel.setForeground(UITheme.ERROR_COLOR);

            JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);

            System.err.println("Error saving preferences: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        String name = authorNameField.getText().trim();
        if (!name.isEmpty() && name.length() < 2) {
            JOptionPane.showMessageDialog(this,
                "Author name must be at least 2 characters long.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        String email = authorEmailField.getText().trim();
        if (!email.isEmpty() && !email.contains("@")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int fileSize = (Integer) maxFileSizeSpinner.getValue();
        if (!prefManager.validateFileSize(fileSize)) {
            JOptionPane.showMessageDialog(this,
                "File size must be between 1 and 10000 MB.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int interval = (Integer) autoRefreshIntervalSpinner.getValue();
        if (!prefManager.validateRefreshInterval(interval)) {
            JOptionPane.showMessageDialog(this,
                "Refresh interval must be between 1 and 60 seconds.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void resetToDefaults() {
        int option = JOptionPane.showConfirmDialog(this,
            "Reset all preferences to default values?\n\nThis cannot be undone.",
            "Reset Preferences",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            prefManager.resetToDefaults();
            loadPreferencesFromManager();
            statusLabel.setText("Reset to defaults");
            statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        }
    }
}

