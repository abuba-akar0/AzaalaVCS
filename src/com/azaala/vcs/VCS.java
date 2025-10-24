package com.azaala.vcs;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Core VCS class that coordinates version control operations.
 * Acts as the facade for the version control system functionality.
 */
public class VCS {
    private Repository repository;
    private FileHandler fileHandler;
    private DiffUtil diffUtil;
    private SummaryGenerator summaryGenerator;

    private static final String DATA_DIR = "data";
    private static final String COMMITS_DIR = "data/commits";
    private static final String INDEX_DIR = "data/index";
    private static final String STAGED_FILES = "data/index/staged_files.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Initializes a new VCS instance.
     */
    public VCS() {
        this.fileHandler = new FileHandler();
        this.diffUtil = new DiffUtil();
        this.summaryGenerator = new SummaryGenerator();
    }

    /**
     * Initializes a new repository at the specified path.
     *
     * @param path Directory path where the repository should be initialized
     * @return true if initialization was successful, false otherwise
     * @throws IllegalArgumentException if path is null or empty
     */
    public boolean initRepository(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository path cannot be null or empty");
        }

        // Validate path for security
        if (!Utils.isValidPath(path)) {
            System.err.println("Invalid repository path: " + path);
            return false;
        }

        String trimmedPath = path.trim();
        File repoDir = new File(trimmedPath);

        try {
            // Create directory if it doesn't exist
            if (!repoDir.exists()) {
                if (!repoDir.mkdirs()) {
                    System.err.println("Failed to create repository directory: " + trimmedPath);
                    return false;
                }
            }

            // Check if directory is writable
            if (!repoDir.canWrite()) {
                System.err.println("Repository directory is not writable: " + trimmedPath);
                return false;
            }

            repository = new Repository(trimmedPath);
            return init();

        } catch (SecurityException e) {
            System.err.println("Security error creating repository: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error initializing repository: " + e.getMessage());
            return false;
        }
    }

    /**
     * Initializes repository folders if they don't exist.
     *
     * @return true if initialization was successful, false otherwise
     */
    public boolean init() {
        if (repository == null) {
            System.err.println("Repository not initialized");
            return false;
        }

        try {
            String basePath = repository.getPath();

            // Create data directory
            File dataDir = new File(basePath, DATA_DIR);
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                System.err.println("Failed to create data directory");
                return false;
            }

            // Create commits directory
            File commitsDir = new File(basePath, COMMITS_DIR);
            if (!commitsDir.exists() && !commitsDir.mkdirs()) {
                System.err.println("Failed to create commits directory");
                return false;
            }

            // Create index directory
            File indexDir = new File(basePath, INDEX_DIR);
            if (!indexDir.exists() && !indexDir.mkdirs()) {
                System.err.println("Failed to create index directory");
                return false;
            }

            // Create staged files tracking file
            File stagedFilesFile = new File(basePath, STAGED_FILES);
            if (!stagedFilesFile.exists()) {
                if (!fileHandler.writeFile(stagedFilesFile.getPath(), "")) {
                    System.err.println("Failed to create staged files tracking file");
                    return false;
                }
            }

            System.out.println("Repository initialized successfully at: " + basePath);
            return true;

        } catch (Exception e) {
            System.err.println("Error during repository initialization: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a file to the staging area and records it in staged_files.txt.
     *
     * @param filePath Path to the file to be added
     * @return true if the file was successfully added, false otherwise
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public boolean addFile(String filePath) {
        if (repository == null) {
            System.err.println("Repository not initialized. Run 'init' first.");
            return false;
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        // Validate and sanitize file path
        String trimmedPath = filePath.trim();
        if (!Utils.isValidPath(trimmedPath)) {
            System.err.println("Invalid file path: " + trimmedPath);
            return false;
        }

        File file = new File(trimmedPath);

        // Check if file exists and is readable
        if (!file.exists()) {
            System.err.println("File does not exist: " + trimmedPath);
            return false;
        }

        if (!file.isFile()) {
            System.err.println("Path is not a file: " + trimmedPath);
            return false;
        }

        if (!file.canRead()) {
            System.err.println("Cannot read file: " + trimmedPath);
            return false;
        }

        // Security check - ensure file is within repository or parent directory
        File repoRoot = new File(repository.getPath());
        // Modified to allow files from parent directories to be added
        if (!Utils.isFileWithinDirectory(file, repoRoot) &&
            !Utils.isFileWithinDirectory(file, repoRoot.getParentFile())) {
            System.err.println("File is outside repository boundaries: " + trimmedPath);
            return false;
        }

        try {
            // Copy file to index
            String indexPath = repository.getPath() + File.separator + INDEX_DIR;
            if (!fileHandler.copyToIndex(trimmedPath, indexPath)) {
                System.err.println("Failed to copy file to index");
                return false;
            }

            // Add to staged files list using Repository method
            if (!repository.stageFile(trimmedPath)) {
                System.err.println("Failed to stage file");
                return false;
            }

            String relativePath = Utils.getRelativePath(file, repoRoot);
            System.out.println("File added to staging area: " + relativePath);
            return true;

        } catch (Exception e) {
            System.err.println("Error adding file '" + trimmedPath + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a new commit with the given message.
     * Creates a snapshot of staged files and metadata.
     *
     * @param message Commit message
     * @return The ID of the new commit if successful, null otherwise
     * @throws IllegalArgumentException if message is null or empty
     */
    public String commit(String message) {
        if (repository == null) {
            System.err.println("Repository not initialized. Run 'init' first.");
            return null;
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Commit message cannot be null or empty");
        }

        String trimmedMessage = message.trim();
        if (trimmedMessage.length() > 500) {
            System.err.println("Commit message too long (max 500 characters)");
            return null;
        }

        try {
            // Get staged files
            List<String> stagedFiles = repository.getStagedFiles();
            if (stagedFiles.isEmpty()) {
                System.err.println("No files staged for commit. Use 'add' to stage files first.");
                return null;
            }

            // Generate commit ID and timestamp
            String commitId = Utils.generateUniqueId();
            LocalDateTime timestamp = LocalDateTime.now();

            // Get previous commit for summary generation
            List<Commit> existingCommits = repository.getCommits();
            Commit previousCommit = existingCommits.isEmpty() ? null : existingCommits.get(existingCommits.size() - 1);

            // Create commit object
            Commit commit = new Commit(commitId, trimmedMessage, timestamp, stagedFiles);

            // Generate summary
            String summary = summaryGenerator.generateSummary(commit, previousCommit);
            commit.setSummary(summary);

            // Save commit to repository
            String commitResult = repository.createCommit(commit);
            if (commitResult == null) {
                System.err.println("Failed to save commit to repository");
                return null;
            }

            // Create commit directory and save files
            String commitPath = repository.getPath() + File.separator + COMMITS_DIR + File.separator + "commit_" + commitId;
            File commitDir = new File(commitPath);
            if (!commitDir.mkdirs()) {
                System.err.println("Failed to create commit directory: " + commitPath);
                return null;
            }

            // Copy staged files to commit directory
            String indexPath = repository.getPath() + File.separator + INDEX_DIR;
            for (String filePath : stagedFiles) {
                File sourceFile = new File(indexPath, Utils.getFileName(filePath));
                File targetFile = new File(commitDir, Utils.getFileName(filePath));
                if (!fileHandler.copyFile(sourceFile.getPath(), targetFile.getPath())) {
                    System.err.println("Failed to copy file to commit: " + filePath);
                    return null;
                }
            }

            // Save commit metadata
            if (!fileHandler.saveCommit(commit, commitPath)) {
                System.err.println("Failed to save commit metadata");
                return null;
            }

            // Clear staged files
            if (!repository.clearStagedFiles()) {
                System.err.println("Warning: Failed to clear staged files");
            }

            System.out.println("Commit created successfully: " + commitId);
            System.out.println("Files committed: " + stagedFiles.size());
            System.out.println("Summary: " + summary);

            return commitId;

        } catch (Exception e) {
            System.err.println("Error creating commit: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the current status of the repository.
     *
     * @return A list of status messages (never null)
     */
    public List<String> getStatus() {
        List<String> status = new ArrayList<>();

        if (repository == null) {
            status.add("Repository not initialized. Run 'init' first.");
            return status;
        }

        try {
            // Check if repository is initialized
            if (!repository.isInitialized()) {
                status.add("Repository structure not properly initialized");
                return status;
            }

            // Get staged files
            List<String> stagedFiles = repository.getStagedFiles();

            // Get repository statistics
            List<Commit> commits = repository.getCommits();

            status.add("=== Repository Status ===");
            status.add("Repository path: " + repository.getPath());
            status.add("Total commits: " + commits.size());
            status.add("Staged files: " + stagedFiles.size());

            if (!stagedFiles.isEmpty()) {
                status.add("\nStaged files:");
                for (String file : stagedFiles) {
                    status.add("  + " + file);
                }
            }

            if (!commits.isEmpty()) {
                Commit latestCommit = commits.get(commits.size() - 1);
                status.add("\nLatest commit:");
                status.add("  ID: " + latestCommit.getCommitId());
                status.add("  Message: " + latestCommit.getMessage());
                status.add("  Date: " + latestCommit.getTimestamp().format(TIMESTAMP_FORMATTER));
            }

            // Quick status summary
            String quickStatus = summaryGenerator.generateQuickStatus(repository);
            status.add("\n" + quickStatus);

        } catch (Exception e) {
            status.add("Error getting repository status: " + e.getMessage());
        }

        return status;
    }

    /**
     * Displays a list of all commits in the repository.
     * Reads information from stored metadata files.
     *
     * @return A list of commit information strings (never null)
     */
    public List<String> log() {
        List<String> logEntries = new ArrayList<>();

        if (repository == null) {
            logEntries.add("Repository not initialized. Run 'init' first.");
            return logEntries;
        }

        try {
            List<Commit> commits = repository.getCommits();

            if (commits.isEmpty()) {
                logEntries.add("No commits found in repository.");
                return logEntries;
            }

            logEntries.add("=== Commit Log ===");
            logEntries.add("Total commits: " + commits.size());
            logEntries.add("");

            // Display commits in reverse chronological order (newest first)
            for (int i = commits.size() - 1; i >= 0; i--) {
                Commit commit = commits.get(i);
                logEntries.add("Commit: " + commit.getCommitId());
                logEntries.add("Date: " + commit.getTimestamp().format(TIMESTAMP_FORMATTER));
                logEntries.add("Message: " + commit.getMessage());
                logEntries.add("Files: " + commit.getFileCount());
                logEntries.add("Summary: " + commit.getSummary());
                logEntries.add("Changed files:");
                for (String file : commit.getChangedFiles()) {
                    logEntries.add("  - " + file);
                }
                logEntries.add("----------------------------");
            }

        } catch (Exception e) {
            logEntries.add("Error reading commit log: " + e.getMessage());
        }

        return logEntries;
    }

    /**
     * Compares two commits and generates a diff report.
     *
     * @param commitId1 First commit ID
     * @param commitId2 Second commit ID
     * @return A list of differences between the commits (never null)
     * @throws IllegalArgumentException if either commit ID is null or empty
     */
    public List<String> diff(String commitId1, String commitId2) {
        List<String> differences = new ArrayList<>();

        if (repository == null) {
            differences.add("Repository not initialized. Run 'init' first.");
            return differences;
        }

        if (commitId1 == null || commitId1.trim().isEmpty()) {
            throw new IllegalArgumentException("First commit ID cannot be null or empty");
        }

        if (commitId2 == null || commitId2.trim().isEmpty()) {
            throw new IllegalArgumentException("Second commit ID cannot be null or empty");
        }

        String trimmedId1 = commitId1.trim();
        String trimmedId2 = commitId2.trim();

        try {
            // Get commits directly from repository
            Commit commit1 = repository.getCommitById(trimmedId1);
            Commit commit2 = repository.getCommitById(trimmedId2);

            if (commit1 == null) {
                differences.add("Commit not found: " + trimmedId1);
                return differences;
            }

            if (commit2 == null) {
                differences.add("Commit not found: " + trimmedId2);
                return differences;
            }

            // Use DiffUtil for detailed comparison
            return diffUtil.compareCommits(commit1, commit2);

        } catch (Exception e) {
            differences.add("Error generating diff: " + e.getMessage());
        }

        return differences;
    }

    /**
     * Gets the current repository instance.
     *
     * @return The repository instance, or null if not initialized
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Checks if the VCS is properly initialized.
     *
     * @return true if repository is initialized, false otherwise
     */
    public boolean isInitialized() {
        return repository != null && repository.isInitialized();
    }

    /**
     * Gets a summary of recent activity in the repository.
     *
     * @param limit Maximum number of commits to include in summary
     * @return Activity summary string
     */
    public String getActivitySummary(int limit) {
        if (repository == null) {
            return "Repository not initialized";
        }

        try {
            List<Commit> commits = repository.getCommits();
            return summaryGenerator.generateCommitSummary(commits, Math.max(1, limit));
        } catch (Exception e) {
            return "Error generating activity summary: " + e.getMessage();
        }
    }
}
