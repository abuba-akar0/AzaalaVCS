package com.azaala.vcs.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.persistence.DatabaseManager;
import com.azaala.vcs.persistence.DatabaseException;

/**
 * Main GUI Dashboard for Azaala VCS
 * Provides graphical interface for all version control operations
 */
public class Dashboard extends JFrame {

    private VCS vcs;
    private Repository repository;
    private RepositoryManager repoManager;
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

        // Initialize database manager first
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initialize();
            System.out.println("✓ Database initialized successfully");
        } catch (DatabaseException e) {
            System.err.println("✗ Database initialization failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                "Warning: Database initialization failed.\n" +
                "Some features may not work correctly.\n\n" +
                "Error: " + e.getMessage(),
                "Database Error",
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            System.err.println("✗ Unexpected error during database initialization: " + e.getMessage());
            e.printStackTrace();
        }

        this.vcs = new VCS();
        this.repoManager = new RepositoryManager();
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
        setSize(1366, 768);
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

        toolBar.addSeparator(new Dimension(UITheme.SPACING_SECTION / 2, 0));

        JButton btnAddAll = new JButton("➕➕ Add All Files");
        btnAddAll.addActionListener(e -> handleAddAllFiles());
        btnAddAll.setToolTipText("Recursively stage all files in repository directory");
        UITheme.stylePrimaryButton(btnAddAll);
        toolBar.add(btnAddAll);

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

        // Add Database Status Panel
        DatabaseStatusPanel dbStatusPanel = new DatabaseStatusPanel();
        tabbedPane.addTab("💾 Database", dbStatusPanel);

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

        // Left side: Repository label
        repositoryLabel = new JLabel("Repository: Not initialized");
        UITheme.styleLabel(repositoryLabel);

        // Center: Database status
        JLabel dbStatusLabel = new JLabel();
        UITheme.styleLabel(dbStatusLabel);
        updateDatabaseStatusLabel(dbStatusLabel);

        // Right side: General status
        statusLabel = new JLabel("Ready");
        UITheme.styleLabel(statusLabel);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(UITheme.PANEL_BACKGROUND);
        leftPanel.add(repositoryLabel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UITheme.PANEL_BACKGROUND);
        centerPanel.add(dbStatusLabel, BorderLayout.CENTER);

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(centerPanel, BorderLayout.CENTER);
        statusBar.add(statusLabel, BorderLayout.EAST);

        mainPanel.add(statusBar, BorderLayout.SOUTH);
    }

    private void updateDatabaseStatusLabel(JLabel dbStatusLabel) {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (dbManager.isInitialized()) {
                String poolInfo = "Database: ✓ Connected (" +
                    dbManager.getConnectionPool().getPoolSize() + " active, " +
                    dbManager.getConnectionPool().getIdleConnections() + " idle)";
                dbStatusLabel.setText(poolInfo);
                dbStatusLabel.setForeground(new Color(34, 139, 34)); // Green
            } else {
                dbStatusLabel.setText("Database: ✗ Not Connected");
                dbStatusLabel.setForeground(new Color(178, 34, 34)); // Red
            }
        } catch (Exception e) {
            dbStatusLabel.setText("Database: ⚠ Error");
            dbStatusLabel.setForeground(new Color(255, 140, 0)); // Orange
        }
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
        // Use folder picker dialog
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("Select Folder to Initialize as Repository");
        folderChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = folderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            String path = folderChooser.getSelectedFile().getAbsolutePath();
            String repoName = new File(path).getName();

            // Create progress listener
            com.azaala.vcs.async.ProgressListener progressListener = new com.azaala.vcs.async.ProgressListener() {
                @Override
                public void onProgress(String message, int progress) {
                    statusLabel.setText(message + " (" + progress + "%)");
                }

                @Override
                public void onSuccess(String result) {
                    try {
                        repository = new Repository(path);
                        repository.setRepoId(null); // Will be set by DAO when persisted
                        vcs.setRepository(repository);
                        repoManager.addRepository(path);

                        overviewPanel.setRepository(repository);
                        historyPanel.setRepository(repository);
                        statusPanel.setRepository(repository);
                        diffPanel.setRepository(repository);
                        settingsPanel.setRepository(repository);

                        updateRepositoryLabel();
                        refreshAllTabs();
                        JOptionPane.showMessageDialog(Dashboard.this,
                            "✓ Repository initialized successfully!\n\nPath: " + path,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(Dashboard.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                @Override
                public void onError(String message, Throwable exception) {
                    statusLabel.setText("Error: " + message);
                    JOptionPane.showMessageDialog(Dashboard.this,
                        "Failed to initialize repository:\n" + message,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            };

            // Execute async worker
            com.azaala.vcs.async.RepositoryInitWorker worker =
                new com.azaala.vcs.async.RepositoryInitWorker(vcs, path, repoName, "Repository initialized", progressListener);
            worker.execute();
        }
    }

    private void handleAddFile() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Initialize a repository first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        // Set initial directory to the repository path
        fileChooser.setCurrentDirectory(new File(repository.getPath()));
        fileChooser.setDialogTitle("Select File to Add to Repository");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();

            // Check if file is already staged
            if (repository.isFileStaged(filePath)) {
                JOptionPane.showMessageDialog(this,
                    "ℹ File already added to staging area!\n\n" + filePath,
                    "File Already Staged",
                    JOptionPane.INFORMATION_MESSAGE);
                if (statusPanel != null) statusPanel.refresh();
                statusLabel.setText("File already staged: " + new File(filePath).getName());
                return;
            }

            // Create progress listener
            com.azaala.vcs.async.ProgressListener progressListener = new com.azaala.vcs.async.ProgressListener() {
                @Override
                public void onProgress(String message, int progress) {
                    statusLabel.setText(message + " (" + progress + "%)");
                }

                @Override
                public void onSuccess(String result) {
                    if (statusPanel != null) statusPanel.refresh();
                    statusLabel.setText("File added successfully!");
                    JOptionPane.showMessageDialog(Dashboard.this, "✓ File added to staging area!");
                }

                @Override
                public void onError(String message, Throwable exception) {
                    statusLabel.setText("Error: " + message);
                    JOptionPane.showMessageDialog(Dashboard.this,
                        "Failed to add file:\n" + message, "Error", JOptionPane.ERROR_MESSAGE);
                }
            };

            // Execute async worker with database integration
            com.azaala.vcs.async.AddFileWorker worker =
                new com.azaala.vcs.async.AddFileWorker(vcs, repository, filePath, progressListener);
            worker.execute();
        }
    }

    /**
     * Handle adding all files from the repository directory recursively.
     * Preserves directory structure and excludes repository metadata folders.
     */
    private void handleAddAllFiles() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Initialize a repository first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm action with user
        String repoPath = repository.getPath();
        String dirInfo = new com.azaala.vcs.FileHandler().getDirectoryStructureInfo(repoPath);

        int confirmResult = JOptionPane.showConfirmDialog(
            this,
            "Add all files from repository?\n\n" +
            "Path: " + repoPath + "\n" +
            "Structure: " + dirInfo + "\n\n" +
            "This will recursively add all files in subdirectories\n" +
            "while preserving folder structure.\n\n" +
            "Excluded: .azaala, .git, .svn, data, target, build, .idea, .vscode",
            "Add All Files",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        // Create progress listener for batch operation
        com.azaala.vcs.async.ProgressListener progressListener = new com.azaala.vcs.async.ProgressListener() {
            @Override
            public void onProgress(String message, int progress) {
                statusLabel.setText(message + " (" + progress + "%)");
            }

            @Override
            public void onSuccess(String result) {
                try {
                    int filesAdded = Integer.parseInt(result);
                    if (filesAdded == 0) {
                        statusLabel.setText("All files already staged!");
                        if (statusPanel != null) statusPanel.refresh();

                        JOptionPane.showMessageDialog(Dashboard.this,
                            "ℹ All files in the repository are already staged!\n\n" +
                            "No new files were added.",
                            "All Files Already Staged",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("Files added successfully! (" + filesAdded + " files)");
                        if (statusPanel != null) statusPanel.refresh();

                        JOptionPane.showMessageDialog(Dashboard.this,
                            "✓ Batch file addition complete!\n\n" +
                            "Files added to staging area: " + filesAdded,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    statusLabel.setText("Batch operation completed");
                    if (statusPanel != null) statusPanel.refresh();
                }
            }

            @Override
            public void onError(String message, Throwable exception) {
                statusLabel.setText("Error: " + message);
                JOptionPane.showMessageDialog(Dashboard.this,
                    "Failed to add all files:\n" + message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                exception.printStackTrace();
            }
        };

        // Execute async worker for batch file addition with directory structure preservation
        com.azaala.vcs.async.AddAllFilesWorker worker =
            new com.azaala.vcs.async.AddAllFilesWorker(vcs, repository, repository.getPath(), progressListener);
        worker.execute();
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
            // Create progress listener
            com.azaala.vcs.async.ProgressListener progressListener = new com.azaala.vcs.async.ProgressListener() {
                @Override
                public void onProgress(String message, int progress) {
                    statusLabel.setText(message + " (" + progress + "%)");
                }

                @Override
                public void onSuccess(String result) {
                    statusLabel.setText("Commit created: " + result);
                    refreshAllTabs();
                    JOptionPane.showMessageDialog(Dashboard.this,
                        "Commit created successfully!\n\nID: " + result);
                }

                @Override
                public void onError(String message, Throwable exception) {
                    statusLabel.setText("Error: " + message);
                    JOptionPane.showMessageDialog(Dashboard.this,
                        "Failed to create commit:\n" + message,
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            };

            // Execute async worker with database transaction management
            com.azaala.vcs.async.CommitWorker worker =
                new com.azaala.vcs.async.CommitWorker(vcs, repository, message, progressListener);
            worker.execute();
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
        // Step 1: Ask user where to create the repository
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("Select Location to Create New Repository");
        folderChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        folderChooser.setAcceptAllFileFilterUsed(false);

        int folderResult = folderChooser.showOpenDialog(this);

        if (folderResult != JFileChooser.APPROVE_OPTION) {
            // User cancelled folder selection
            return;
        }

        String parentPath = folderChooser.getSelectedFile().getAbsolutePath();

        // Step 2: Create a custom dialog for repository details
        JDialog newRepoDialog = new JDialog(this, "Create New Repository", true);
        newRepoDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        newRepoDialog.setSize(500, 350);
        newRepoDialog.setLocationRelativeTo(this);
        newRepoDialog.setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Create New Repository");
        titleLabel.setFont(UITheme.SUBTITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Parent path display
        JLabel parentPathLabel = new JLabel("Location:");
        parentPathLabel.setFont(UITheme.LABEL_FONT);
        mainPanel.add(parentPathLabel);

        JLabel parentPathValueLabel = new JLabel(parentPath);
        parentPathValueLabel.setFont(UITheme.CONTENT_FONT);
        parentPathValueLabel.setForeground(UITheme.TEXT_SECONDARY);
        mainPanel.add(parentPathValueLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Repository name
        JLabel nameLabel = new JLabel("Repository Name:");
        nameLabel.setFont(UITheme.LABEL_FONT);
        mainPanel.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setFont(UITheme.CONTENT_FONT);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        nameField.setPreferredSize(new Dimension(400, 35));
        UITheme.styleTextField(nameField);
        mainPanel.add(nameField);
        mainPanel.add(Box.createVerticalStrut(15));

        // Repository description (optional)
        JLabel descLabel = new JLabel("Description (Optional):");
        descLabel.setFont(UITheme.LABEL_FONT);
        mainPanel.add(descLabel);

        JTextArea descArea = new JTextArea(3, 40);
        descArea.setFont(UITheme.CONTENT_FONT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        UITheme.styleTextArea(descArea);
        JScrollPane descScroll = new JScrollPane(descArea);
        mainPanel.add(descScroll);
        mainPanel.add(Box.createVerticalStrut(15));

        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(UITheme.BACKGROUND_COLOR);

        JCheckBox createGitIgnoreCheckBox = new JCheckBox("Create .gitignore file");
        createGitIgnoreCheckBox.setSelected(true);
        createGitIgnoreCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(createGitIgnoreCheckBox);
        optionsPanel.add(createGitIgnoreCheckBox);

        JCheckBox createReadmeCheckBox = new JCheckBox("Create README.md file");
        createReadmeCheckBox.setSelected(true);
        createReadmeCheckBox.setBackground(UITheme.BACKGROUND_COLOR);
        UITheme.styleCheckBox(createReadmeCheckBox);
        optionsPanel.add(createReadmeCheckBox);

        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UITheme.BACKGROUND_COLOR);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 35));
        cancelBtn.addActionListener(e -> newRepoDialog.dispose());
        UITheme.stylePrimaryButton(cancelBtn);
        buttonPanel.add(cancelBtn);

        JButton createBtn = new JButton("Create Repository");
        createBtn.setPreferredSize(new Dimension(150, 35));
        createBtn.addActionListener(e -> {
            String repoName = nameField.getText().trim();
            String description = descArea.getText().trim();
            boolean createGitIgnore = createGitIgnoreCheckBox.isSelected();
            boolean createReadme = createReadmeCheckBox.isSelected();

            newRepoDialog.dispose();
            createNewRepository(parentPath, repoName, description, createGitIgnore, createReadme);
        });
        UITheme.styleSuccessButton(createBtn);
        buttonPanel.add(createBtn);

        mainPanel.add(buttonPanel);

        newRepoDialog.add(mainPanel);
        newRepoDialog.setVisible(true);
    }

    /**
     * Create a new repository with specified parameters
     */
    private void createNewRepository(String parentPath, String repoName, String description,
                                    boolean createGitIgnore, boolean createReadme) {
        // Validate repository name
        if (repoName == null || repoName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Repository name cannot be empty.\n\nPlease enter a valid repository name.",
                "Invalid Name",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate repository name (no special characters)
        if (!repoName.matches("^[a-zA-Z0-9_-]+$")) {
            JOptionPane.showMessageDialog(this,
                "Repository name can only contain letters, numbers, hyphens, and underscores.\n\n" +
                "Invalid characters detected.",
                "Invalid Characters",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create full path
        String fullPath = parentPath + File.separator + repoName;
        File repoDir = new File(fullPath);

        // Check if directory already exists
        if (repoDir.exists()) {
            int option = JOptionPane.showConfirmDialog(this,
                "A folder named '" + repoName + "' already exists.\n\n" +
                "Would you like to initialize it as a repository anyway?",
                "Directory Exists",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            // Initialize repository using VCS
            if (!vcs.initRepository(fullPath)) {
                JOptionPane.showMessageDialog(this,
                    "Failed to initialize repository.\n\n" +
                    "Please check:\n" +
                    "- The path is writable\n" +
                    "- You have permission to create directories\n" +
                    "- Sufficient disk space available",
                    "Initialization Failed",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create optional files
            if (createGitIgnore) {
                createGitIgnoreFile(fullPath);
            }

            if (createReadme) {
                createReadmeFile(fullPath, repoName, description);
            }

            // Load the newly created repository
            loadRepository(fullPath);

            // Show success message
            JOptionPane.showMessageDialog(this,
                "✓ New repository created successfully!\n\n" +
                "Repository Name: " + repoName + "\n" +
                "Path: " + fullPath + "\n\n" +
                "Repository is now ready to use.\n" +
                "You can start adding files and creating commits.",
                "Repository Created",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error creating repository:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Create .gitignore file with common patterns
     */
    private void createGitIgnoreFile(String repoPath) {
        try {
            String gitIgnoreContent = "# Java\n" +
                "*.class\n" +
                "*.jar\n" +
                "*.war\n" +
                "bin/\n" +
                "target/\n" +
                ".classpath\n" +
                ".project\n" +
                "\n" +
                "# IDE\n" +
                ".vscode/\n" +
                ".idea/\n" +
                "*.iml\n" +
                "*.swp\n" +
                "*.swo\n" +
                "\n" +
                "# OS\n" +
                ".DS_Store\n" +
                "Thumbs.db\n" +
                "\n" +
                "# Logs\n" +
                "*.log\n" +
                "logs/\n";

            File gitIgnoreFile = new File(repoPath, ".gitignore");
            java.nio.file.Files.write(gitIgnoreFile.toPath(), gitIgnoreContent.getBytes());
        } catch (Exception e) {
            System.err.println("Warning: Could not create .gitignore file: " + e.getMessage());
        }
    }

    /**
     * Create README.md file with repository information
     */
    private void createReadmeFile(String repoPath, String repoName, String description) {
        try {
            String readmeContent = "# " + repoName + "\n\n" +
                (description != null && !description.isEmpty() ? description + "\n\n" : "") +
                "## Getting Started\n\n" +
                "This is a version control repository managed by Azaala VCS.\n\n" +
                "### Basic Commands\n\n" +
                "1. **Add Files**\n" +
                "   - Use the \"Add File\" button to stage files for commit\n\n" +
                "2. **Create Commits**\n" +
                "   - Use the \"Commit\" button to save staged changes\n\n" +
                "3. **View History**\n" +
                "   - Use the \"Commit History\" tab to see all commits\n\n" +
                "4. **Compare Changes**\n" +
                "   - Use the \"Diff Viewer\" tab to compare commits\n\n" +
                "## Repository Structure\n\n" +
                "```\n" +
                repoName + "/\n" +
                "├── data/\n" +
                "│   ├── commits/       (Commit snapshots)\n" +
                "│   ├── index/         (Staging area)\n" +
                "│   └── commits.log    (Commit history)\n" +
                "├── .gitignore         (Files to ignore)\n" +
                "├── README.md          (This file)\n" +
                "└── [your files]\n" +
                "```\n\n" +
                "## Tips\n\n" +
                "- Create commits frequently with meaningful messages\n" +
                "- Use the diff viewer to review changes before committing\n" +
                "- Check file status to understand what's staged and what's not\n\n" +
                "---\n" +
                "Created with Azaala VCS v1.0.0\n";

            File readmeFile = new File(repoPath, "README.md");
            java.nio.file.Files.write(readmeFile.toPath(), readmeContent.getBytes());
        } catch (Exception e) {
            System.err.println("Warning: Could not create README.md file: " + e.getMessage());
        }
    }

    private void showOpenRepositoryDialog() {
        List<String> savedRepos = repoManager.getRepositories();

        if (savedRepos.isEmpty()) {
            // No saved repos, ask user to browse
            int option = JOptionPane.showConfirmDialog(
                this,
                "No saved repositories found.\n\nWould you like to browse for a repository?",
                "Open Repository",
                JOptionPane.YES_NO_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                browseForRepository();
            }
            return;
        }

        // Create a dialog to select from saved repositories
        String[] repoArray = savedRepos.toArray(new String[0]);
        String selectedRepo = (String) JOptionPane.showInputDialog(
            this,
            "Select a repository to open:",
            "Open Repository",
            JOptionPane.PLAIN_MESSAGE,
            null,
            repoArray,
            repoArray[0]
        );

        if (selectedRepo != null) {
            File repoDir = new File(selectedRepo);
            if (repoDir.exists() && repoDir.isDirectory()) {
                loadRepository(selectedRepo);
            } else {
                // Repository path no longer exists
                int option = JOptionPane.showConfirmDialog(
                    this,
                    "The repository path no longer exists:\n" + selectedRepo + "\n\n" +
                    "Would you like to remove it from the list and browse for another?",
                    "Repository Not Found",
                    JOptionPane.YES_NO_OPTION
                );

                if (option == JOptionPane.YES_OPTION) {
                    repoManager.removeRepository(selectedRepo);
                    browseForRepository();
                }
            }
        }
    }

    /**
     * Browse for a repository folder
     */
    private void browseForRepository() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setDialogTitle("Select Repository Folder");

        int result = dirChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String path = dirChooser.getSelectedFile().getAbsolutePath();
            loadRepository(path);
        }
    }

    /**
     * Load a repository at the given path
     */
    private void loadRepository(String path) {
        try {
            repository = new Repository(path);
            vcs.setRepository(repository);

            // Try to load repository ID from database
            try {
                com.azaala.vcs.persistence.dao.RepositoryDAO repoDAO =
                    new com.azaala.vcs.persistence.dao.RepositoryDAO();
                com.azaala.vcs.persistence.models.RepositoryEntity repoEntity =
                    repoDAO.findByPath(path);

                if (repoEntity != null) {
                    // Repository exists in database
                    repository.setRepoId(repoEntity.getRepoId());
                    System.out.println("✓ Repository ID loaded from database: " + repoEntity.getRepoId());
                } else {
                    // Repository doesn't exist in database - register it now
                    System.out.println("⚠ Repository not found in database. Registering now...");
                    com.azaala.vcs.persistence.models.RepositoryEntity newRepo =
                        new com.azaala.vcs.persistence.models.RepositoryEntity();
                    newRepo.setRepoName(repository.getName());
                    newRepo.setRepoPath(path);
                    newRepo.setCreatedAt(java.time.LocalDateTime.now());
                    newRepo.setLastCommitAt(java.time.LocalDateTime.now());

                    Long repoId = repoDAO.create(newRepo);
                    if (repoId != null) {
                        repository.setRepoId(repoId);
                        System.out.println("✓ Repository registered in database with ID: " + repoId);
                    } else {
                        System.out.println("⚠ Failed to register repository in database");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠ Database error: " + e.getMessage());
                System.out.println("Continuing without database integration for this session");
            }

            // Save to quick access list
            repoManager.addRepository(path);

            // Update all panels with the loaded repository instance
            overviewPanel.setRepository(repository);
            historyPanel.setRepository(repository);
            statusPanel.setRepository(repository);
            diffPanel.setRepository(repository);
            settingsPanel.setRepository(repository);

            updateRepositoryLabel();
            refreshAllTabs();
            JOptionPane.showMessageDialog(this,
                "✓ Repository loaded successfully!\n\nPath: " + path,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to load repository:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPreferencesDialog() {
        // Launch the professional preferences dialog
        new PreferencesDialog(this);
    }

    private void showAboutDialog() {
        String aboutText =
            "╔═══════════════════════════════════════════════════════════╗\n" +
            "║          AZAALA VCS - Version 1.0.0              ║\n" +
            "║     Lightweight Version Control System           ║\n" +
            "║          © 2025 - All Rights Reserved            ║\n" +
            "╚═══════════════════════════════════════════════════════════╝\n\n" +

            "📋 SYSTEM INFORMATION:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "• Application: Azaala Version Control System\n" +
            "• Version: 1.0.0\n" +
            "• Java Version: " + System.getProperty("java.version") + "\n" +
            "• Operating System: " + System.getProperty("os.name") + "\n" +
            "• Architecture: " + System.getProperty("os.arch") + "\n\n" +

            "🏗️  ARCHITECTURE:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "• Frontend: Java Swing GUI (Modern, Responsive)\n" +
            "• Backend: Pure Java VCS Engine\n" +
            "• Database: MySQL 8.0 (HikariCP Connection Pool)\n" +
            "• Threading: SwingWorker (Non-blocking Operations)\n" +
            "• Storage: HYBRID (Database + File System)\n\n" +

            "💾 HYBRID STORAGE APPROACH:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "DATABASE STORES:              FILESYSTEM STORES:\n" +
            "✓ Repository metadata        ✓ File contents\n" +
            "✓ Commit records             ✓ Commit snapshots\n" +
            "✓ Staging area               ✓ Working directory\n" +
            "✓ File references            ✓ Index files\n" +
            "✓ Activity/Audit logs        ✓ Configuration\n\n" +

            "✨ KEY FEATURES:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "✓ Initialize repositories\n" +
            "✓ Stage files (single & batch operations)\n" +
            "✓ Create commits with messages\n" +
            "✓ View complete commit history\n" +
            "✓ Compare any two commits (Diff)\n" +
            "✓ Track file changes and activity\n" +
            "✓ Support multiple repositories\n" +
            "✓ Automatic database initialization\n\n" +

            "🔄 SYNCHRONIZATION:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "The hybrid approach ensures:\n" +
            "• Database provides fast searches & relationships\n" +
            "• Filesystem provides efficient storage\n" +
            "• Both stay synchronized during operations\n" +
            "• Graceful fallback if database unavailable\n" +
            "• Audit trail in database for compliance\n\n" +

            "🔧 COMPONENTS:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Core Modules:\n" +
            "  • VCS Engine - Repository & commit management\n" +
            "  • FileHandler - File operations & copying\n" +
            "  • DiffUtil - Change detection & comparison\n" +
            "  • DatabaseManager - MySQL connectivity\n\n" +

            "GUI Panels:\n" +
            "  • Overview - Repository information\n" +
            "  • Status - Staged & tracked files\n" +
            "  • History - Commit log viewer\n" +
            "  • Diff - Commit comparison\n" +
            "  • Settings - Configuration\n\n" +

            "📊 DATABASE TABLES:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "• repositories - Repository metadata\n" +
            "• commits - Commit history\n" +
            "• staged_files - Current staging area\n" +
            "• commit_files - Files in each commit\n" +
            "• activity_logs - Audit trail\n\n" +

            "🚀 PERFORMANCE:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "• Connection Pool: 10 max connections\n" +
            "• Query Optimization: Indexed lookups\n" +
            "• Async Operations: Non-blocking UI\n" +
            "• Efficient Storage: Hybrid approach\n\n" +

            "📝 FOR MORE INFORMATION:\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Visit: https://github.com/abuba-akar0/AzaalaVCS\n" +
            "Docs: See README.md in project root\n";

        // Create styled text area with larger font
        JTextArea textArea = new JTextArea(aboutText);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 22));
        textArea.setBackground(new Color(245, 245, 250)); // Light background
        textArea.setForeground(new Color(30, 30, 40)); // Dark text
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new java.awt.Insets(15, 15, 15, 15));
        textArea.setCaretPosition(0);

        // Create scrollable panel - NO fixed size, let content determine width
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(245, 245, 250));

        // Create custom dialog
        JDialog aboutDialog = new JDialog(this, "About Azaala VCS", true);
        aboutDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        aboutDialog.setResizable(true);

        // Main panel with colored background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 250));

        // Header panel with gradient-like appearance
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185)); // Blue
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        headerPanel.setPreferredSize(new java.awt.Dimension(800, 60)); // Wide to match text

        JLabel headerLabel = new JLabel("Azaala VCS - About");
        headerLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(245, 245, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 240, 245));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 210)));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        closeButton.setPreferredSize(new java.awt.Dimension(100, 35));
        closeButton.setBackground(new Color(41, 128, 185));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        closeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> aboutDialog.dispose());
        buttonPanel.add(closeButton);

        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        aboutDialog.add(mainPanel);

        // Set optimal size - wide to match text content
        aboutDialog.setSize(800, 700);

        // Center on screen
        aboutDialog.setLocationRelativeTo(this);

        aboutDialog.setVisible(true);
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

