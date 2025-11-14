package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;
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

    public SettingsPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        setLayout(new GridLayout(5, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("Default Commit Message:"));
        txtDefaultCommitMsg = new JTextField();
        add(txtDefaultCommitMsg);

        add(new JLabel("Max File Size (MB):"));
        txtMaxFileSize = new JTextField("100");
        add(txtMaxFileSize);

        add(new JLabel("Auto-refresh:"));
        chkAutoRefresh = new JCheckBox();
        add(chkAutoRefresh);

        add(new JLabel());
        JButton btnSave = new JButton("Save Settings");
        btnSave.addActionListener(e -> saveSettings());
        add(btnSave);
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    private void saveSettings() {
        JOptionPane.showMessageDialog(this, "Settings saved!");
    }

    public void refresh() {
        // TODO: Load and display current settings
    }
}
