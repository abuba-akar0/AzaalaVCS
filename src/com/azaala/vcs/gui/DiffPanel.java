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

    public DiffPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        setLayout(new BorderLayout());

        JPanel selectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectPanel.add(new JLabel("Compare:"));
        commit1Combo = new JComboBox<>();
        selectPanel.add(commit1Combo);
        selectPanel.add(new JLabel("with"));
        commit2Combo = new JComboBox<>();
        selectPanel.add(commit2Combo);

        JButton btnCompare = new JButton("Compare");
        btnCompare.addActionListener(e -> performDiff());
        selectPanel.add(btnCompare);

        add(selectPanel, BorderLayout.NORTH);

        diffArea = new JTextArea();
        diffArea.setEditable(false);
        diffArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(diffArea), BorderLayout.CENTER);
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    private void performDiff() {
        String commit1 = (String) commit1Combo.getSelectedItem();
        String commit2 = (String) commit2Combo.getSelectedItem();

        if (commit1 != null && commit2 != null) {
            diffArea.setText("Diff between " + commit1 + " and " + commit2 + "\n(Implementation pending)");
        }
    }

    public void refresh() {
        commit1Combo.removeAllItems();
        commit2Combo.removeAllItems();

        if (repository != null) {
            for (var commit : repository.getCommits()) {
                commit1Combo.addItem(commit.getCommitId());
                commit2Combo.addItem(commit.getCommitId());
            }
        }
    }
}