package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;

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

        setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                                  UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));

        // Add info panel
        JPanel infoPanel = UITheme.createInfoPanel(
            "Diff Viewer",
            "Compare differences between two commits. " +
            "Select two commits from the dropdowns and click 'Compare' to see what changed. " +
            "Useful for reviewing code changes and understanding project history."
        );
        add(infoPanel, BorderLayout.NORTH);

        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Create diff display area
        JPanel diffPanel = new JPanel(new BorderLayout(UITheme.SPACING_COMPONENT, UITheme.SPACING_COMPONENT));
        diffPanel.setBackground(UITheme.BACKGROUND_COLOR);

        diffArea = new JTextArea();
        diffArea.setEditable(false);
        UITheme.styleTextArea(diffArea);
        diffArea.setText("Select two commits and click 'Compare' to view differences.");

        JScrollPane scrollPane = new JScrollPane(diffArea);
        scrollPane.setBorder(UITheme.createStyledBorder("Diff Results"));
        diffPanel.add(scrollPane, BorderLayout.CENTER);

        // Stats panel
        JPanel statsPanel = new JPanel();
        statsPanel.setBackground(UITheme.PANEL_BACKGROUND);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM,
                                                             UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM));
        statsLabel = new JLabel("No comparison yet");
        UITheme.styleLabel(statsLabel);
        statsPanel.add(statsLabel);
        diffPanel.add(statsPanel, BorderLayout.SOUTH);

        add(diffPanel, BorderLayout.CENTER);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, UITheme.SPACING_SECTION, UITheme.SPACING_COMPONENT));
        panel.setBackground(UITheme.PANEL_BACKGROUND);
        panel.setBorder(UITheme.createStyledBorder("Select Commits"));

        JLabel label1 = new JLabel("Compare:");
        UITheme.styleLabel(label1);
        panel.add(label1);

        commit1Combo = new JComboBox<>();
        UITheme.styleComboBox(commit1Combo);
        panel.add(commit1Combo);

        JLabel label2 = new JLabel("with");
        UITheme.styleLabel(label2);
        panel.add(label2);

        commit2Combo = new JComboBox<>();
        UITheme.styleComboBox(commit2Combo);
        panel.add(commit2Combo);

        JButton btnCompare = new JButton("⇄ Compare");
        btnCompare.addActionListener(e -> performDiff());
        UITheme.stylePrimaryButton(btnCompare);
        panel.add(btnCompare);

        return panel;
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    private void performDiff() {
        String commit1 = (String) commit1Combo.getSelectedItem();
        String commit2 = (String) commit2Combo.getSelectedItem();

        if (commit1 == null || commit2 == null) {
            diffArea.setText("ERROR: Please select two commits to compare");
            statsLabel.setText("No comparison");
            return;
        }

        if (commit1.equals(commit2)) {
            diffArea.setText("INFO: Same commit selected. No differences.");
            statsLabel.setText("Comparing: " + commit1 + " vs " + commit2 + " (identical)");
            return;
        }

        try {
            // TODO: Call actual VCS.diff() method when available
            // For now, show placeholder with proper formatting
            StringBuilder diffOutput = new StringBuilder();
            diffOutput.append("═══════════════════════════════════════════════\n");
            diffOutput.append("Comparing: ").append(commit1).append(" → ").append(commit2).append("\n");
            diffOutput.append("═══════════════════════════════════════════════\n\n");
            diffOutput.append("Diff Implementation Details:\n");
            diffOutput.append("───────────────────────────────────────────────\n");
            diffOutput.append("The diff functionality will show:\n");
            diffOutput.append("  • Files added (marked with +)\n");
            diffOutput.append("  • Files removed (marked with -)\n");
            diffOutput.append("  • Files modified (marked with ~)\n");
            diffOutput.append("  • Line-by-line differences for each file\n\n");
            diffOutput.append("Integration with VCS.diff() is pending.\n");
            diffOutput.append("═══════════════════════════════════════════════\n");

            diffArea.setText(diffOutput.toString());
            statsLabel.setText("Commits: " + commit1 + " → " + commit2);
            diffArea.setCaretPosition(0);

        } catch (Exception e) {
            diffArea.setText("ERROR: " + e.getMessage());
            statsLabel.setText("Error comparing commits");
        }
    }

    public void refresh() {
        commit1Combo.removeAllItems();
        commit2Combo.removeAllItems();

        if (repository != null && repository.getCommits() != null) {
            for (var commit : repository.getCommits()) {
                commit1Combo.addItem(commit.getCommitId());
                commit2Combo.addItem(commit.getCommitId());
            }
        }

        diffArea.setText("Select two commits and click 'Compare' to view differences.");
        statsLabel.setText("No comparison yet");
    }
}