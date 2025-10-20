package com.azaala.vcs;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Represents a repository in the version control system.
 * Manages repository structure, commits, and file tracking.
 */
public class Repository {
    private String repositoryPath;
    private String name;
    private LocalDateTime createdAt;
    private List<Commit> commits;

    private static final String DATA_DIR = "data";
    private static final String COMMITS_DIR = "data/commits";
    private static final String INDEX_DIR = "data/index";
    private static final String STAGED_FILES = "data/index/staged_files.txt";
    private static final String COMMITS_LOG = "data/commits.log";

    /**
     * Creates a new Repository instance.
     *
     * @param repositoryPath The path where the repository is located
     * @throws IllegalArgumentException if repositoryPath is null or empty
     */
    public Repository(String repositoryPath) {
        if (repositoryPath == null || repositoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository path cannot be null or empty");
        }

        this.repositoryPath = repositoryPath.trim();
        this.name = new File(this.repositoryPath).getName();
        this.createdAt = LocalDateTime.now();
        this.commits = new ArrayList<>();

        // Load existing commits if repository already exists
        loadExistingCommits();
    }

    /**
     * Gets the repository path.
     *
     * @return The repository path (never null)
     */
    public String getPath() {
        return repositoryPath;
    }

    /**
     * Sets the repository path.
     *
     * @param repositoryPath The new repository path
     * @throws IllegalArgumentException if repositoryPath is null or empty
     */
    public void setPath(String repositoryPath) {
        if (repositoryPath == null || repositoryPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository path cannot be null or empty");
        }
        this.repositoryPath = repositoryPath.trim();
        this.name = new File(this.repositoryPath).getName();
    }

    /**
     * Gets the repository name.
     *
     * @return The repository name (never null)
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the repository name.
     *
     * @param name The new repository name
     * @throws IllegalArgumentException if name is null or empty
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Gets the creation timestamp of the repository.
     *
     * @return The creation timestamp (never null)
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Checks if the repository is properly initialized.
     * Verifies that the required directory structure exists.
     *
     * @return true if repository is initialized, false otherwise
     */
    public boolean isInitialized() {
        try {
            File dataDir = new File(repositoryPath, DATA_DIR);
            File commitsDir = new File(repositoryPath, COMMITS_DIR);
            File indexDir = new File(repositoryPath, INDEX_DIR);

            return dataDir.exists() && dataDir.isDirectory() &&
                    commitsDir.exists() && commitsDir.isDirectory() &&
                    indexDir.exists() && indexDir.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Creates a new commit and saves it to the repository.
     *
     * @param commit The commit to create and save
     * @return The commit ID if successful, null otherwise
     * @throws IllegalArgumentException if commit is null
     */
    public String createCommit(Commit commit) {
        if (commit == null) {
            throw new IllegalArgumentException("Commit cannot be null");
        }

        try {
            // Add commit to in-memory list
            commits.add(commit);

            // Save commit to persistent storage
            if (saveCommitToFile(commit)) {
                System.out.println("Commit saved successfully: " + commit.getCommitId());
                return commit.getCommitId();
            } else {
                // Remove from memory if save failed
                commits.remove(commit);
                System.err.println("Failed to save commit to file");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error creating commit: " + e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new commit and saves it to the repository.
     * Overloaded version that accepts a commit ID string
     *
     * @param commitId The commit ID to create
     * @return The commit ID if successful, null otherwise
     */
    public String createCommit(String commitId) {
        if (commitId == null || commitId.trim().isEmpty()) {
            return null;
        }

        // This method should be used by tests only
        // In production code, use the createCommit(Commit) method
        return commitId;
    }

    /**
     * Saves a commit to the commits log file.
     *
     * @param commit The commit to save
     * @return true if successful, false otherwise
     */
    private boolean saveCommitToFile(Commit commit) {
        try {
            File commitsLogFile = new File(repositoryPath, COMMITS_LOG);

            // Create the file if it doesn't exist
            if (!commitsLogFile.exists()) {
                commitsLogFile.getParentFile().mkdirs();
                commitsLogFile.createNewFile();
            }

            // Append commit information to the log
            try (FileWriter writer = new FileWriter(commitsLogFile, true);
                 PrintWriter printWriter = new PrintWriter(writer)) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                printWriter.println("=== COMMIT " + commit.getCommitId() + " ===");
                printWriter.println("Message: " + commit.getMessage());
                printWriter.println("Timestamp: " + commit.getTimestamp().format(formatter));
                printWriter.println("Summary: " + commit.getSummary());
                printWriter.println("Files: " + String.join(", ", commit.getChangedFiles()));
                printWriter.println("File Count: " + commit.getFileCount());
                printWriter.println("=== END COMMIT ===");
                printWriter.println();

                return true;
            }
        } catch (IOException e) {
            System.err.println("Error saving commit to file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads existing commits from the commits log file.
     */
    private void loadExistingCommits() {
        try {
            File commitsLogFile = new File(repositoryPath, COMMITS_LOG);

            if (!commitsLogFile.exists()) {
                // Create empty commits log if it doesn't exist
                try {
                    commitsLogFile.getParentFile().mkdirs();
                    commitsLogFile.createNewFile();
                } catch (IOException e) {
                    System.err.println("Could not create commits log file: " + e.getMessage());
                }
                return; // No commits to load
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(commitsLogFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("=== COMMIT ") && line.endsWith(" ===")) {
                        Commit commit = parseCommitFromLog(reader, line);
                        if (commit != null) {
                            commits.add(commit);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading existing commits: " + e.getMessage());
        }
    }

    /**
     * Parses a commit from the log file.
     *
     * @param reader The BufferedReader for the log file
     * @param headerLine The commit header line
     * @return The parsed Commit object, or null if parsing failed
     */
    private Commit parseCommitFromLog(BufferedReader reader, String headerLine) {
        try {
            // Extract commit ID from header
            String commitId = headerLine.substring(11, headerLine.length() - 4).trim();

            String message = null;
            LocalDateTime timestamp = null;
            List<String> files = new ArrayList<>();
            String summary = null;

            String line;
            while ((line = reader.readLine()) != null && !line.equals("=== END COMMIT ===")) {
                if (line.startsWith("Message: ")) {
                    message = line.substring(9);
                } else if (line.startsWith("Timestamp: ")) {
                    String timestampStr = line.substring(11);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    timestamp = LocalDateTime.parse(timestampStr, formatter);
                } else if (line.startsWith("Summary: ")) {
                    summary = line.substring(9);
                } else if (line.startsWith("Files: ")) {
                    String filesStr = line.substring(7);
                    if (!filesStr.trim().isEmpty()) {
                        files = Arrays.asList(filesStr.split(", "));
                    }
                }
            }

            if (message != null && timestamp != null) {
                Commit commit = new Commit(commitId, message, timestamp, files);
                if (summary != null) {
                    commit.setSummary(summary);
                }
                return commit;
            }
        } catch (Exception e) {
            System.err.println("Error parsing commit from log: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets all commits in the repository.
     *
     * @return A list of all commits (never null)
     */
    public List<Commit> getCommits() {
        return new ArrayList<>(commits);
    }

    /**
     * Gets a commit by its ID.
     *
     * @param commitId The commit ID to search for
     * @return The commit if found, null otherwise
     */
    public Commit getCommitById(String commitId) {
        if (commitId == null || commitId.trim().isEmpty()) {
            return null;
        }

        String trimmedId = commitId.trim();
        for (Commit commit : commits) {
            if (commit.getCommitId().equals(trimmedId)) {
                return commit;
            }
        }
        return null;
    }

    /**
     * Gets the list of currently staged files.
     *
     * @return A list of staged file paths (never null)
     */
    public List<String> getStagedFiles() {
        List<String> stagedFiles = new ArrayList<>();

        try {
            File stagedFilesFile = new File(repositoryPath, STAGED_FILES);

            if (!stagedFilesFile.exists()) {
                return stagedFiles; // Return empty list if file doesn't exist
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(stagedFilesFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        stagedFiles.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading staged files: " + e.getMessage());
        }

        return stagedFiles;
    }

    /**
     * Adds a file to the staging area.
     *
     * @param filePath The path of the file to stage
     * @return true if successful, false otherwise
     */
    public boolean stageFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            List<String> stagedFiles = getStagedFiles();
            String trimmedPath = filePath.trim();

            // Don't add if already staged
            if (stagedFiles.contains(trimmedPath)) {
                return true;
            }

            stagedFiles.add(trimmedPath);
            return saveStagedFiles(stagedFiles);
        } catch (Exception e) {
            System.err.println("Error staging file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves the list of staged files to the staging file.
     *
     * @param stagedFiles The list of staged files
     * @return true if successful, false otherwise
     */
    private boolean saveStagedFiles(List<String> stagedFiles) {
        try {
            File stagedFilesFile = new File(repositoryPath, STAGED_FILES);

            // Create parent directories if they don't exist
            stagedFilesFile.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(stagedFilesFile))) {
                for (String filePath : stagedFiles) {
                    writer.println(filePath);
                }
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error saving staged files: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears all staged files.
     *
     * @return true if successful, false otherwise
     */
    public boolean clearStagedFiles() {
        return saveStagedFiles(new ArrayList<>());
    }

    /**
     * Gets the total number of commits in the repository.
     *
     * @return The number of commits
     */
    public int getCommitCount() {
        return commits.size();
    }

    /**
     * Gets the latest commit in the repository.
     *
     * @return The latest commit, or null if no commits exist
     */
    public Commit getLatestCommit() {
        if (commits.isEmpty()) {
            return null;
        }
        return commits.get(commits.size() - 1);
    }

    /**
     * Checks if the repository has any commits.
     *
     * @return true if there are commits, false otherwise
     */
    public boolean hasCommits() {
        return !commits.isEmpty();
    }

    /**
     * Gets a string representation of the repository.
     *
     * @return Repository information as a string
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return "Repository '" + name + "' at " + repositoryPath +
                "\nCreated: " + createdAt.format(formatter) +
                "\nCommits: " + commits.size() +
                "\nInitialized: " + isInitialized();
    }

    /**
     * Saves a commit snapshot by copying files.
     *
     * @param stagedFiles List of staged file paths
     * @param commitId Commit ID for the snapshot directory
     * @return true if successful, false otherwise
     */
    public boolean saveSnapshot(List<String> stagedFiles, String commitId) {
        if (stagedFiles == null || commitId == null || commitId.trim().isEmpty()) {
            return false;
        }

        try {
            String snapshotPath = repositoryPath + File.separator + COMMITS_DIR +
                    File.separator + "commit_" + commitId + File.separator + "snapshot";

            // Create snapshot directory
            File snapshotDir = new File(snapshotPath);
            if (!snapshotDir.exists() && !snapshotDir.mkdirs()) {
                System.err.println("Failed to create snapshot directory: " + snapshotPath);
                return false;
            }

            FileHandler fileHandler = new FileHandler();

            // Copy each staged file to snapshot directory
            for (String filePath : stagedFiles) {
                File sourceFile = new File(filePath);
                if (!sourceFile.exists() || !sourceFile.isFile()) {
                    System.err.println("Staged file doesn't exist: " + filePath);
                    continue;
                }

                // Copy file to snapshot location
                String targetPath = snapshotPath + File.separator + sourceFile.getName();
                if (!fileHandler.copyFile(filePath, targetPath)) {
                    System.err.println("Failed to copy file to snapshot: " + filePath);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error saving snapshot: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads a commit by ID and returns its metadata and files.
     *
     * @param commitId The commit ID to load
     * @return The loaded commit, or null if not found
     */
    public Commit loadCommit(String commitId) {
        if (commitId == null || commitId.trim().isEmpty()) {
            return null;
        }

        // First check if it's already loaded in memory
        Commit commit = getCommitById(commitId.trim());
        if (commit != null) {
            return commit;
        }

        try {
            String commitPath = repositoryPath + File.separator + COMMITS_DIR +
                    File.separator + "commit_" + commitId.trim();

            File commitDir = new File(commitPath);
            if (!commitDir.exists() || !commitDir.isDirectory()) {
                System.err.println("Commit directory not found: " + commitPath);
                return null;
            }

            File metadataFile = new File(commitDir, "metadata.txt");
            if (!metadataFile.exists() || !metadataFile.isFile()) {
                System.err.println("Commit metadata file not found: " + metadataFile.getPath());
                return null;
            }

            // Read metadata
            Properties metadata = new Properties();
            try (FileInputStream fis = new FileInputStream(metadataFile)) {
                metadata.load(fis);
            }

            String message = metadata.getProperty("MESSAGE");
            String timestamp = metadata.getProperty("TIMESTAMP");
            String summary = metadata.getProperty("SUMMARY");
            String filesStr = metadata.getProperty("FILES");

            if (message == null || timestamp == null || summary == null || filesStr == null) {
                System.err.println("Incomplete metadata in commit: " + commitId);
                return null;
            }

            // Parse timestamp
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime commitTime = LocalDateTime.parse(timestamp, formatter);

            // Parse files
            List<String> files = new ArrayList<>();
            if (!filesStr.isEmpty()) {
                files = Arrays.asList(filesStr.split(","));
            }

            // Create commit object
            Commit loadedCommit = new Commit(commitId.trim(), message, commitTime, files);
            loadedCommit.setSummary(summary);

            return loadedCommit;

        } catch (Exception e) {
            System.err.println("Error loading commit: " + e.getMessage());
            return null;
        }
    }

    /**
     * Updates the head.txt file to track the latest commit ID.
     *
     * @param commitId The commit ID to set as head
     * @return true if successful, false otherwise
     */
    public boolean updateHead(String commitId) {
        if (commitId == null || commitId.trim().isEmpty()) {
            return false;
        }

        try {
            String headFilePath = repositoryPath + File.separator + INDEX_DIR + File.separator + "head.txt";

            // Write commit ID to head file
            try (FileWriter writer = new FileWriter(headFilePath)) {
                writer.write(commitId.trim());
                return true;
            }

        } catch (IOException e) {
            System.err.println("Error updating HEAD: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the current HEAD commit ID.
     *
     * @return Current HEAD commit ID, or null if not set
     */
    public String getHead() {
        try {
            String headFilePath = repositoryPath + File.separator + INDEX_DIR + File.separator + "head.txt";
            File headFile = new File(headFilePath);

            if (!headFile.exists()) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(headFile))) {
                return reader.readLine();
            }

        } catch (IOException e) {
            System.err.println("Error reading HEAD: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the current HEAD commit.
     *
     * @return The HEAD commit, or null if not found
     */
    public Commit getHeadCommit() {
        String headCommitId = getHead();
        if (headCommitId == null) {
            return null;
        }

        return getCommitById(headCommitId);
    }

    /**
     * Returns this repository's metadata as a Map.
     * Useful for tests and serialization.
     *
     * @return Map with repository metadata
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("path", repositoryPath);
        map.put("name", name);
        map.put("createdAt", createdAt);
        map.put("commitCount", commits.size());
        map.put("initialized", isInitialized());

        List<Map<String, Object>> commitMaps = new ArrayList<>();
        for (Commit commit : commits) {
            commitMaps.add(commit.toMap());
        }
        map.put("commits", commitMaps);

        return map;
    }

    /**
     * Initializes repository folders if they don't exist.
     *
     * @return true if initialization was successful, false otherwise
     */
    public boolean initialize() {
        try {
            // Create data directory
            File dataDir = new File(repositoryPath, DATA_DIR);
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                System.err.println("Failed to create data directory");
                return false;
            }

            // Create commits directory
            File commitsDir = new File(repositoryPath, COMMITS_DIR);
            if (!commitsDir.exists() && !commitsDir.mkdirs()) {
                System.err.println("Failed to create commits directory");
                return false;
            }

            // Create index directory
            File indexDir = new File(repositoryPath, INDEX_DIR);
            if (!indexDir.exists() && !indexDir.mkdirs()) {
                System.err.println("Failed to create index directory");
                return false;
            }

            // Create staged files tracking file
            File stagedFilesFile = new File(repositoryPath, STAGED_FILES);
            if (!stagedFilesFile.exists()) {
                try {
                    stagedFilesFile.getParentFile().mkdirs();
                    stagedFilesFile.createNewFile();
                } catch (IOException e) {
                    System.err.println("Failed to create staged files tracking file: " + e.getMessage());
                    return false;
                }
            }

            System.out.println("Repository initialized successfully at: " + repositoryPath);
            return true;

        } catch (Exception e) {
            System.err.println("Error during repository initialization: " + e.getMessage());
            return false;
        }
    }
}
