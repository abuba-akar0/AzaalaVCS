package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;

/**
 * Overview Panel - Displays repository statistics and information
 */
public class OverviewPanel extends JPanel {

    private VCS vcs;
    private Repository repository;

    private JLabel lblRepoName;
    private JLabel lblRepoPath;
    private JLabel lblCreatedDate;
    private JLabel lblTotalCommits;
    private JLabel lblStagedFiles;
    private JLabel lblTrackedFiles;

    public OverviewPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                                  UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));

        // Add info panel at top
        JPanel infoPanel = UITheme.createInfoPanel(
            "Repository Overview",
            "View key statistics and information about your repository. " +
            "Shows the total number of commits, staged files, and tracked files. " +
            "Use this tab to quickly assess your repository status."
        );
        add(infoPanel, BorderLayout.NORTH);

        // Create stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, UITheme.SPACING_SECTION, UITheme.SPACING_SECTION));
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_LARGE, UITheme.PADDING_LARGE,
                                                        UITheme.PADDING_LARGE, UITheme.PADDING_LARGE));

        // Row 1: Repo Name and Path
        panel.add(createStatItem("Repository Name:", lblRepoName = createValueLabel("N/A")));
        panel.add(createStatItem("Path:", lblRepoPath = createValueLabel("N/A")));

        // Row 2: Created Date and Total Commits
        panel.add(createStatItem("Created:", lblCreatedDate = createValueLabel("N/A")));
        panel.add(createStatItem("Total Commits:", lblTotalCommits = createValueLabel("0")));

        // Row 3: Staged Files and Tracked Files
        panel.add(createStatItem("Staged Files:", lblStagedFiles = createValueLabel("0")));
        panel.add(createStatItem("Tracked Files:", lblTrackedFiles = createValueLabel("0")));

        return panel;
    }

    private JPanel createStatItem(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(UITheme.SPACING_COMPONENT, 0));
        panel.setBackground(UITheme.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                           UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM)
        ));

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.TEXT_PRIMARY);

        panel.add(label, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        UITheme.styleTitleLabel(label);
        label.setForeground(UITheme.PRIMARY_COLOR);
        return label;
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    public void refresh() {
        if (repository != null) {
            lblRepoName.setText(repository.getName());
            lblRepoPath.setText(repository.getPath());
            lblCreatedDate.setText(repository.getCreatedAt().toString());
            lblTotalCommits.setText(String.valueOf(repository.getCommits().size()));
            lblStagedFiles.setText("0");
            lblTrackedFiles.setText(String.valueOf(repository.getCommits().stream()
                .mapToInt(c -> c.getFileCount())
                .sum()));
        } else {
            lblRepoName.setText("No repository");
            lblRepoPath.setText("N/A");
            lblCreatedDate.setText("N/A");
            lblTotalCommits.setText("0");
            lblStagedFiles.setText("0");
            lblTrackedFiles.setText("0");
        }
    }
}
