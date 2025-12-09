package com.azaala.vcs.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;

/**
 * Main GUI Dashboard for Azaala VCS
 * Provides graphical interface for all version control operations
 */
public class Dashboard extends JFrame {

    private VCS vcs;
    private Repository repository;
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JLabel repositoryLabel;
    private JLabel statusLabel;

    private OverviewPanel overviewPanel;
    private HistoryPanel historyPanel;
    private StatusPanel statusPanel;
    private DiffPanel diffPanel;
    private SettingsPanel settingsPanel;

    private JButton btnInit;
    private JButton btnAdd;
    private JButton btnCommit;
    private JButton btnDiff;
    private JButton btnRefresh;

    public Dashboard() {
        // Apply theme before creating components
        UITheme.applyTheme();

        this.vcs = new VCS();
        initializeFrame();
        buildMenuBar();
        buildToolBar();
        buildTabbedPane();
        buildStatusBar();
        setupListeners();
        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("Azaala VCS - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(true);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM,
                                           UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        setContentPane(mainPanel);
    }

    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(UITheme.PANEL_BACKGROUND);
        menuBar.setFont(UITheme.LABEL_FONT);

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(UITheme.LABEL_FONT);
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem miNewRepo = new JMenuItem("New Repository");
        miNewRepo.setFont(UITheme.CONTENT_FONT);
        miNewRepo.addActionListener(e -> showNewRepositoryDialog());
        fileMenu.add(miNewRepo);

        JMenuItem miOpenRepo = new JMenuItem("Open Repository");
        miOpenRepo.setFont(UITheme.CONTENT_FONT);
        miOpenRepo.addActionListener(e -> showOpenRepositoryDialog());
        fileMenu.add(miOpenRepo);

        fileMenu.addSeparator();

        JMenuItem miExit = new JMenuItem("Exit");
        miExit.setFont(UITheme.CONTENT_FONT);
        miExit.addActionListener(e -> System.exit(0));
        fileMenu.add(miExit);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setFont(UITheme.LABEL_FONT);
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem miPreferences = new JMenuItem("Preferences");
        miPreferences.setFont(UITheme.CONTENT_FONT);
        miPreferences.addActionListener(e -> showPreferencesDialog());
        editMenu.add(miPreferences);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(UITheme.LABEL_FONT);
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem miRefresh = new JMenuItem("Refresh");
        miRefresh.setFont(UITheme.CONTENT_FONT);
        miRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        miRefresh.addActionListener(e -> refreshAllTabs());
        viewMenu.add(miRefresh);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(UITheme.LABEL_FONT);
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem miAbout = new JMenuItem("About");
        miAbout.setFont(UITheme.CONTENT_FONT);
        miAbout.addActionListener(e -> showAboutDialog());
        helpMenu.add(miAbout);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(UITheme.PANEL_BACKGROUND);
        toolBar.setRollover(true);
        toolBar.setBorder(BorderFactory.createEmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_SMALL,
                                                          UITheme.PADDING_SMALL, UITheme.PADDING_SMALL));

        btnInit = new JButton("📁 Initialize");
        btnInit.addActionListener(e -> handleInitRepository());
        btnInit.setToolTipText("Create new VCS repository in selected directory");
        UITheme.stylePrimaryButton(btnInit);
        toolBar.add(btnInit);

        toolBar.addSeparator(new Dimension(UITheme.SPACING_SECTION, 0));

        btnAdd = new JButton("➕ Add File");
        btnAdd.addActionListener(e -> handleAddFile());
        btnAdd.setToolTipText("Stage files for the next commit");
        UITheme.stylePrimaryButton(btnAdd);
        toolBar.add(btnAdd);

        toolBar.addSeparator(new Dimension(UITheme.SPACING_SECTION, 0));

        btnCommit = new JButton("✓ Commit");
        btnCommit.addActionListener(e -> handleCommit());
        btnCommit.setToolTipText("Save staged changes to repository history");
        UITheme.styleSuccessButton(btnCommit);
        toolBar.add(btnCommit);

        toolBar.addSeparator(new Dimension(UITheme.SPACING_SECTION * 2, 0));

        btnDiff = new JButton("⇄ View Diff");
        btnDiff.addActionListener(e -> handleViewDiff());
        btnDiff.setToolTipText("Compare two commits to see what changed");
        UITheme.stylePrimaryButton(btnDiff);
        toolBar.add(btnDiff);

        toolBar.addSeparator(new Dimension(UITheme.SPACING_SECTION, 0));

        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshAllTabs());
        btnRefresh.setToolTipText("Update all tabs with latest repository data");
        UITheme.stylePrimaryButton(btnRefresh);
        toolBar.add(btnRefresh);

        mainPanel.add(toolBar, BorderLayout.NORTH);
    }

    private void buildTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        tabbedPane.setBackground(UITheme.BACKGROUND_COLOR);
        tabbedPane.setFont(UITheme.LABEL_FONT);

        overviewPanel = new OverviewPanel(vcs, repository);
        tabbedPane.addTab("📊 Overview", overviewPanel);

        historyPanel = new HistoryPanel(vcs, repository);
        tabbedPane.addTab("📜 Commit History", historyPanel);

        statusPanel = new StatusPanel(vcs, repository);
        tabbedPane.addTab("📁 File Status", statusPanel);

        diffPanel = new DiffPanel(vcs, repository);
        tabbedPane.addTab("⇄ Diff", diffPanel);

        settingsPanel = new SettingsPanel(vcs, repository);
        tabbedPane.addTab("⚙ Settings", settingsPanel);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private void buildStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout(UITheme.PADDING_MEDIUM, UITheme.PADDING_MEDIUM));
        statusBar.setBackground(UITheme.PANEL_BACKGROUND);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            new EtchedBorder(EtchedBorder.LOWERED),
            new EmptyBorder(UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM,
                           UITheme.PADDING_SMALL, UITheme.PADDING_MEDIUM)
        ));

        repositoryLabel = new JLabel("Repository: Not initialized");
        UITheme.styleLabel(repositoryLabel);

        statusLabel = new JLabel("Ready");
        UITheme.styleLabel(statusLabel);

        statusBar.add(repositoryLabel, BorderLayout.WEST);
        statusBar.add(statusLabel, BorderLayout.EAST);

        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch(selectedIndex) {
                case 0:
                    if (overviewPanel != null) overviewPanel.refresh();
                    break;
                case 1:
                    if (historyPanel != null) historyPanel.refresh();
                    break;
                case 2:
                    if (statusPanel != null) statusPanel.refresh();
                    break;
                case 3:
                    if (diffPanel != null) diffPanel.refresh();
                    break;
                case 4:
                    if (settingsPanel != null) settingsPanel.refresh();
                    break;
            }
        });
    }

    private void handleInitRepository() {
        String path = JOptionPane.showInputDialog(
            this,
            "Enter repository path:",
            System.getProperty("user.home")
        );

        if (path != null && !path.trim().isEmpty()) {
            if (vcs.initRepository(path)) {
                try {
                    repository = new Repository(path);

                    // Update all panels with the new repository instance
                    overviewPanel.setRepository(repository);
                    historyPanel.setRepository(repository);
                    statusPanel.setRepository(repository);
                    diffPanel.setRepository(repository);
                    settingsPanel.setRepository(repository);

                    updateRepositoryLabel();
                    refreshAllTabs();
                    JOptionPane.showMessageDialog(this, "Repository initialized successfully!");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to initialize repository", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAddFile() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Initialize a repository first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (vcs.addFile(selectedFile.getAbsolutePath())) {
                if (statusPanel != null) statusPanel.refresh();
                JOptionPane.showMessageDialog(this, "File added to staging area!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCommit() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Initialize a repository first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = JOptionPane.showInputDialog(
            this,
            "Enter commit message:"
        );

        if (message != null && !message.trim().isEmpty()) {
            String commitId = vcs.commit(message);
            if (commitId != null) {
                refreshAllTabs();
                JOptionPane.showMessageDialog(this, "Commit created successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create commit", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleViewDiff() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Initialize a repository first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tabbedPane.setSelectedIndex(3);
    }

    private void showNewRepositoryDialog() {
        JOptionPane.showMessageDialog(this, "New Repository dialog - to be implemented");
    }

    private void showOpenRepositoryDialog() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = dirChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = dirChooser.getSelectedFile().getAbsolutePath();
            try {
                repository = new Repository(path);

                // Update all panels with the loaded repository instance
                overviewPanel.setRepository(repository);
                historyPanel.setRepository(repository);
                statusPanel.setRepository(repository);
                diffPanel.setRepository(repository);
                settingsPanel.setRepository(repository);

                updateRepositoryLabel();
                refreshAllTabs();
                JOptionPane.showMessageDialog(this, "Repository loaded successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to load repository: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPreferencesDialog() {
        JOptionPane.showMessageDialog(this, "Preferences dialog - to be implemented");
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Azaala VCS - Version 1.0.0\n" +
            "A lightweight version control system\n" +
            "© 2025",
            "About Azaala VCS",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshAllTabs() {
        updateRepositoryLabel();
        if (overviewPanel != null) overviewPanel.refresh();
        if (historyPanel != null) historyPanel.refresh();
        if (statusPanel != null) statusPanel.refresh();
        if (diffPanel != null) diffPanel.refresh();
        if (settingsPanel != null) settingsPanel.refresh();
        statusLabel.setText("Refreshed at " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
    }

    private void updateRepositoryLabel() {
        if (repository != null) {
            repositoryLabel.setText("Repository: " + repository.getPath());
        } else {
            repositoryLabel.setText("Repository: Not initialized");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard());
    }
}

