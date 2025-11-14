package com.azaala.vcs.gui;

import javax.swing.*;
import java.awt.*;
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

    public StatusPanel(VCS vcs, Repository repository) {
        this.vcs = vcs;
        this.repository = repository;

        setLayout(new GridLayout(1, 3, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        stagedList = new JList<>();
        trackedList = new JList<>();
        modifiedList = new JList<>();

        add(createListPanel("Staged Files", stagedList));
        add(createListPanel("Tracked Files", trackedList));
        add(createListPanel("Modified Files", modifiedList));
    }

    private JPanel createListPanel(String title, JList<String> list) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    public void setRepository(Repository repo) {
        this.repository = repo;
        refresh();
    }

    public void refresh() {
        // TODO: Populate lists with actual data from repository
    }
}