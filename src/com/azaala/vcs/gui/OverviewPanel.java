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

        setLayout(new GridLayout(6, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("Repository Name:"));
        lblRepoName = new JLabel("N/A");
        add(lblRepoName);

        add(new JLabel("Path:"));
        lblRepoPath = new JLabel("N/A");
        add(lblRepoPath);

        add(new JLabel("Created:"));
        lblCreatedDate = new JLabel("N/A");
        add(lblCreatedDate);

        add(new JLabel("Total Commits:"));
        lblTotalCommits = new JLabel("0");
        add(lblTotalCommits);

        add(new JLabel("Staged Files:"));
        lblStagedFiles = new JLabel("0");
        add(lblStagedFiles);

        add(new JLabel("Tracked Files:"));
        lblTrackedFiles = new JLabel("0");
        add(lblTrackedFiles);
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
