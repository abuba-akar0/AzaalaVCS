package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;

import com.azaala.vcs.DiffUtil;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.Commit;
import java.util.List;

/**
 * Diff Panel - Displays diff comparison between commits
 */
public class DiffPanel extends JPanel {

    private VCS vcs;
    private Repository repository;
    private JComboBox<String> commit1Combo;
    private JComboBox<String> commit2Combo;
    private JTextArea diffArea;
    private JLabel statsLabel;

    public DiffPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        // MAIN LAYOUT: BorderLayout
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // TOP SECTION: Info + Controls
        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setBackground(UITheme.BACKGROUND_COLOR);

        // Info panel
        JPanel infoPanel = UITheme.createInfoPanel(
            "Diff Viewer",
            "Select two commits from the dropdowns below and click COMPARE COMMITS button to see differences."
        );
        topSection.add(infoPanel, BorderLayout.NORTH);

        // Control panel (selection dropdowns and button)
        JPanel controlPanel = createControlPanel();
        topSection.add(controlPanel, BorderLayout.CENTER);

        add(topSection, BorderLayout.NORTH);

        // CENTER SECTION: Diff results display
        diffArea = new JTextArea();
        diffArea.setEditable(false);
        diffArea.setFont(UITheme.MONOSPACE_FONT);
        diffArea.setLineWrap(true);
        diffArea.setWrapStyleWord(true);
        diffArea.setText("Select two commits and click COMPARE COMMITS to view differences.");
        UITheme.styleTextArea(diffArea);

        JScrollPane scrollPane = new JScrollPane(diffArea);
        scrollPane.setBorder(UITheme.createStyledBorder("Diff Results"));
        add(scrollPane, BorderLayout.CENTER);

        // BOTTOM SECTION: Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(UITheme.PANEL_BACKGROUND);
        statsLabel = new JLabel("Ready to compare commits");
        UITheme.styleLabel(statsLabel);
        statusBar.add(statsLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        // Use GridBagLayout for precise control
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.PANEL_BACKGROUND);
        panel.setBorder(UITheme.createStyledBorder("Select Commits to Compare"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ROW 1: Commit 1 Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel label1 = new JLabel("Commit 1 (From):");
        UITheme.styleTitleLabel(label1);
        panel.add(label1, gbc);

        // ROW 1: Commit 1 Dropdown
        gbc.gridx = 1;
        gbc.weightx = 1;
        commit1Combo = new JComboBox<>();
        commit1Combo.setPreferredSize(new Dimension(400, 35));
        UITheme.styleComboBox(commit1Combo);
        panel.add(commit1Combo, gbc);

        // ROW 2: Commit 2 Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel label2 = new JLabel("Commit 2 (To):");
        UITheme.styleTitleLabel(label2);
        panel.add(label2, gbc);

        // ROW 2: Commit 2 Dropdown
        gbc.gridx = 1;
        gbc.weightx = 1;
        commit2Combo = new JComboBox<>();
        commit2Combo.setPreferredSize(new Dimension(400, 35));
        UITheme.styleComboBox(commit2Combo);
        panel.add(commit2Combo, gbc);

        // ROW 3: Compare Button (spanning both columns, centered)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton btnCompare = new JButton("⇄ COMPARE COMMITS");
        btnCompare.setPreferredSize(new Dimension(250, 50));
        btnCompare.setFont(new Font(UITheme.LABEL_FONT.getName(), Font.BOLD, 14));
        btnCompare.addActionListener(e -> performDiff());
        UITheme.styleSuccessButton(btnCompare);
        panel.add(btnCompare, gbc);

        return panel;
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    public void refresh() {
        commit1Combo.removeAllItems();
        commit2Combo.removeAllItems();

        if (repository != null && repository.getCommits() != null && !repository.getCommits().isEmpty()) {
            java.util.List<Commit> commits = repository.getCommits();

            // Add commits with formatted display (ID | Message)
            for (Commit commit : commits) {
                String displayText = formatCommitDisplay(commit);
                commit1Combo.addItem(displayText);
                commit2Combo.addItem(displayText);
            }

            // Auto-select first and last commits
            if (commits.size() > 0) {
                commit1Combo.setSelectedIndex(0);
            }

            if (commits.size() > 1) {
                commit2Combo.setSelectedIndex(commits.size() - 1);
            } else if (commits.size() > 0) {
                commit2Combo.setSelectedIndex(0);
            }

            diffArea.setText("✓ Commits loaded. Select two different commits and click COMPARE COMMITS.");
            statsLabel.setText(String.format("Ready - %d commit(s) available", commits.size()));
        } else {
            diffArea.setText("No commits available.\n\nPlease:\n1. Initialize repository\n2. Add files\n3. Create commits");
            statsLabel.setText("No commits available");
        }
    }

    private String formatCommitDisplay(Commit commit) {
        String id = commit.getCommitId().substring(0, Math.min(8, commit.getCommitId().length()));
        String message = commit.getMessage();
        if (message.length() > 40) {
            message = message.substring(0, 40) + "...";
        }
        return String.format("%s | %s", id, message);
    }

    private String extractCommitId(String displayText) {
        int pipeIndex = displayText.indexOf(" | ");
        if (pipeIndex > 0) {
            return displayText.substring(0, pipeIndex).trim();
        }
        return displayText;
    }

    private void performDiff() {
        // Validate repository
        if (repository == null || repository.getCommits() == null || repository.getCommits().isEmpty()) {
            diffArea.setText("ERROR: No repository or commits available.\n\nPlease initialize repository and create commits first.");
            statsLabel.setText("No commits available");
            return;
        }

        // Get selected items
        Object item1 = commit1Combo.getSelectedItem();
        Object item2 = commit2Combo.getSelectedItem();

        if (item1 == null || item2 == null) {
            diffArea.setText("ERROR: Please select two commits from the dropdowns.");
            statsLabel.setText("No selection");
            return;
        }

        // Extract commit IDs
        String commit1IdPrefix = extractCommitId(item1.toString());
        String commit2IdPrefix = extractCommitId(item2.toString());

        if (commit1IdPrefix.equals(commit2IdPrefix)) {
            diffArea.setText("INFO: Same commit selected.\n\nPlease select TWO DIFFERENT commits to compare.");
            statsLabel.setText("Same commit selected");
            return;
        }

        try {
            // Find commits by ID
            Commit commit1 = null;
            Commit commit2 = null;

            for (Commit commit : repository.getCommits()) {
                if (commit.getCommitId().startsWith(commit1IdPrefix)) {
                    commit1 = commit;
                }
                if (commit.getCommitId().startsWith(commit2IdPrefix)) {
                    commit2 = commit;
                }
            }

            if (commit1 == null || commit2 == null) {
                diffArea.setText("ERROR: Could not find commits.");
                statsLabel.setText("Invalid selection");
                return;
            }

            // Use robust DiffUtil with line-by-line comparison
            DiffUtil diffUtil = new DiffUtil();
            List<String> detailedDiff = diffUtil.generateDetailedDiff(
                commit1,
                commit2,
                repository.getPath()
            );

            // Display comprehensive diff output
            StringBuilder output = new StringBuilder();
            for (String line : detailedDiff) {
                output.append(line).append("\n");
            }

            diffArea.setText(output.toString());

            // Calculate statistics from output
            long additions = detailedDiff.stream().filter(l -> l.contains("  ➕")).count();
            long deletions = detailedDiff.stream().filter(l -> l.contains("  ➖")).count();

            statsLabel.setText(String.format("✓ Detailed analysis: %s → %s | Changes: +%d -%d",
                commit1.getCommitId().substring(0, 8),
                commit2.getCommitId().substring(0, 8),
                additions, deletions));
            diffArea.setCaretPosition(0);

        } catch (Exception e) {
            diffArea.setText("ERROR: " + e.getMessage());
            statsLabel.setText("Error");
            e.printStackTrace();
        }
    }
}

