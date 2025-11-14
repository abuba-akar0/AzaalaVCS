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

        setLayout(new BorderLayout());

        String[] columns = {"Commit ID", "Message", "Date", "Files"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = historyTable.getSelectedRow();
                if (row >= 0) {
                    displayCommitDetails(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setPreferredSize(new Dimension(0, 150));
        add(detailsScroll, BorderLayout.SOUTH);
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
            details.append("Commit ID: ").append(commit.getCommitId()).append("\n");
            details.append("Message: ").append(commit.getMessage()).append("\n");
            details.append("Timestamp: ").append(commit.getTimestamp()).append("\n");
            details.append("Files Changed: ").append(commit.getFileCount()).append("\n\n");
            details.append("Changed Files:\n");
            for (String file : commit.getChangedFiles()) {
                details.append("  - ").append(file).append("\n");
            }
            detailsArea.setText(details.toString());
        }
    }
}
