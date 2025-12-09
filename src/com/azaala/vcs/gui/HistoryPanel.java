package com.azaala.vcs.gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.Commit;

/**
 * History Panel - Displays commit history and details
 */
public class HistoryPanel extends JPanel {

    private VCS vcs;
    private Repository repository;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;

    public HistoryPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                                  UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));

        // Add info panel
        JPanel infoPanel = UITheme.createInfoPanel(
            "Commit History",
            "Browse all commits in your repository. " +
            "Click a commit row to view detailed information including files changed. " +
            "Use this to track project changes over time and understand project evolution."
        );
        add(infoPanel, BorderLayout.NORTH);

        // Create center panel with table and details
        JPanel centerPanel = new JPanel(new BorderLayout(UITheme.SPACING_SECTION, UITheme.SPACING_SECTION));
        centerPanel.setBackground(UITheme.BACKGROUND_COLOR);

        String[] columns = {"Commit ID", "Message", "Date", "Files"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(historyTable);

        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = historyTable.getSelectedRow();
                if (row >= 0) {
                    displayCommitDetails(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(UITheme.createStyledBorder("All Commits"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        UITheme.styleTextArea(detailsArea);

        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setPreferredSize(new Dimension(0, 150));
        detailsScroll.setBorder(UITheme.createStyledBorder("Commit Details"));
        centerPanel.add(detailsScroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    public void refresh() {
        tableModel.setRowCount(0);
        detailsArea.setText("");

        if (repository != null) {
            List<Commit> commits = repository.getCommits();
            // Display commits in reverse order (newest first)
            for (int i = commits.size() - 1; i >= 0; i--) {
                Commit commit = commits.get(i);
                Object[] row = {
                    commit.getCommitId(),
                    commit.getMessage(),
                    commit.getTimestamp().toString(),
                    commit.getFileCount()
                };
                tableModel.addRow(row);
            }
        }
    }

    private void displayCommitDetails(int row) {
        if (repository != null && row >= 0 && row < repository.getCommits().size()) {
            Commit commit = repository.getCommits().get(row);
            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════════\n");
            details.append("Commit ID: ").append(commit.getCommitId()).append("\n");
            details.append("Message: ").append(commit.getMessage()).append("\n");
            details.append("Timestamp: ").append(commit.getTimestamp()).append("\n");
            details.append("Files Changed: ").append(commit.getFileCount()).append("\n");
            details.append("═══════════════════════════════════════════\n\n");
            details.append("Changed Files:\n");
            details.append("───────────────────────────────────────────\n");
            for (String file : commit.getChangedFiles()) {
                details.append("  ✓ ").append(file).append("\n");
            }
            detailsArea.setText(details.toString());
            detailsArea.setCaretPosition(0);
        }
    }
}
