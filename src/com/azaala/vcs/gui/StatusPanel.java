package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;

/**
 * Status Panel - Displays file staging and tracking status
 */
public class StatusPanel extends JPanel {

    private VCS vcs;
    private Repository repository;
    private JList<String> stagedList;
    private JList<String> trackedList;
    private JList<String> modifiedList;

    private DefaultListModel<String> stagedModel;
    private DefaultListModel<String> trackedModel;
    private DefaultListModel<String> modifiedModel;

    public StatusPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setBackground(UITheme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                                  UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));

        // Add info panel
        JPanel infoPanel = UITheme.createInfoPanel(
            "File Status",
            "Monitor file changes in your repository. " +
            "Shows which files are staged for commit, currently tracked, and recently modified. " +
            "Use staging area to prepare files before committing."
        );
        add(infoPanel, BorderLayout.NORTH);

        // Create lists panel
        JPanel listsPanel = new JPanel(new GridLayout(1, 3, UITheme.SPACING_SECTION, UITheme.SPACING_SECTION));
        listsPanel.setBackground(UITheme.BACKGROUND_COLOR);

        stagedModel = new DefaultListModel<>();
        stagedList = new JList<>(stagedModel);
        UITheme.styleList(stagedList);
        listsPanel.add(createListPanel("📌 Staged Files", stagedList, stagedModel));

        trackedModel = new DefaultListModel<>();
        trackedList = new JList<>(trackedModel);
        UITheme.styleList(trackedList);
        listsPanel.add(createListPanel("✓ Tracked Files", trackedList, trackedModel));

        modifiedModel = new DefaultListModel<>();
        modifiedList = new JList<>(modifiedModel);
        UITheme.styleList(modifiedList);
        listsPanel.add(createListPanel("✎ Modified Files", modifiedList, modifiedModel));

        add(listsPanel, BorderLayout.CENTER);
    }

    private JPanel createListPanel(String title, JList<String> list, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND_COLOR);
        panel.setBorder(UITheme.createStyledBorder(title));

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBackground(UITheme.BACKGROUND_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add count label at bottom
        JLabel countLabel = new JLabel("Count: 0");
        UITheme.styleLabel(countLabel);
        countLabel.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_SMALL,
                                                             UITheme.PADDING_SMALL, UITheme.PADDING_SMALL));
        panel.add(countLabel, BorderLayout.SOUTH);

        return panel;
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    public void refresh() {
        stagedModel.clear();
        trackedModel.clear();
        modifiedModel.clear();

        if (repository != null) {
            loadStagedFiles();
            loadTrackedFiles();
            loadModifiedFiles();
        }
    }

    /**
     * Load staged files from the staging index
     */
    private void loadStagedFiles() {
        try {
            String repoPath = repository.getPath();
            Path stagedFilePath = Paths.get(repoPath, "data", "index", "staged_files.txt");

            if (Files.exists(stagedFilePath)) {
                List<String> lines = Files.readAllLines(stagedFilePath);
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        stagedModel.addElement(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading staged files: " + e.getMessage());
        }
    }

    /**
     * Load tracked files from all commits
     */
    private void loadTrackedFiles() {
        Set<String> trackedFiles = new LinkedHashSet<>();

        if (repository != null && repository.getCommits() != null) {
            repository.getCommits().forEach(commit -> {
                if (commit.getChangedFiles() != null) {
                    trackedFiles.addAll(commit.getChangedFiles());
                }
            });
        }

        trackedFiles.stream()
            .sorted()
            .forEach(trackedModel::addElement);
    }

    /**
     * Load modified files by checking working directory against last commit
     */
    private void loadModifiedFiles() {
        Set<String> modifiedFiles = new LinkedHashSet<>();

        try {
            if (repository == null || repository.getPath() == null) {
                return;
            }

            String repoPath = repository.getPath();
            Path repoRootPath = Paths.get(repoPath);

            // Get all files in working directory (excluding .git and data folders)
            Files.walk(repoRootPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String pathStr = path.toString();
                    return !pathStr.contains(".git") && !pathStr.contains(File.separator + "data");
                })
                .forEach(path -> {
                    try {
                        String relativePath = repoRootPath.relativize(path).toString();
                        modifiedFiles.add(relativePath);
                    } catch (Exception e) {
                        // Ignore path resolution errors
                    }
                });
        } catch (IOException e) {
            System.err.println("Error loading modified files: " + e.getMessage());
        }

        modifiedFiles.stream()
            .sorted()
            .forEach(modifiedModel::addElement);
    }
}