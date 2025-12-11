# 📚 Azaala VCS - API Reference & Integration Guide

Complete API documentation and integration guidelines for Azaala VCS.

---

## Table of Contents

1. [Core VCS API](#core-vcs-api)
2. [Repository API](#repository-api)
3. [Commit API](#commit-api)
4. [FileHandler API](#filehandler-api)
5. [Diff & Summary APIs](#diff--summary-apis)
6. [Database API](#database-api)
7. [Async API](#async-api)
8. [GUI API](#gui-api)
9. [Code Examples](#code-examples)
10. [Error Codes](#error-codes)

---

## Core VCS API

### VCS Class

**Package**: `com.azaala.vcs`

#### Constructor

```java
public VCS()
```
- Creates new VCS instance
- Initializes FileHandler, DiffUtil, SummaryGenerator
- Does NOT initialize repository (call initRepository)

#### Repository Initialization

```java
public boolean initRepository(String path)
```
**Parameters**:
- `path` (String): Directory path for new repository

**Returns**: `boolean` - true if successful, false otherwise

**Exceptions**:
- `IllegalArgumentException` - if path is null or empty
- `SecurityException` - if insufficient permissions

**Throws**:
```java
if (path == null || path.trim().isEmpty()) {
    throw new IllegalArgumentException("Path cannot be empty");
}
if (!Utils.isValidPath(path)) {
    return false; // Logs error
}
```

**Example**:
```java
VCS vcs = new VCS();
if (vcs.initRepository("/home/user/myproject")) {
    System.out.println("Repository initialized");
} else {
    System.err.println("Failed to initialize");
}
```

#### File Operations

```java
public boolean addFile(String filePath)
```
**Parameters**:
- `filePath` (String): Path to file to stage

**Returns**: `boolean` - true if added, false otherwise

**Behavior**:
1. Validates file exists
2. Checks file is within repository
3. Copies to staging area
4. Returns success status

**Example**:
```java
if (vcs.addFile("src/Main.java")) {
    System.out.println("File staged");
}
```

```java
public boolean addAllFiles(String directoryPath)
```
**Parameters**:
- `directoryPath` (String): Path to directory

**Returns**: `boolean` - true if all files added, false otherwise

**Example**:
```java
vcs.addAllFiles("src/");
```

```java
public List<String> getStagedFiles()
```
**Returns**: `List<String>` - paths of staged files, empty if none

**Example**:
```java
List<String> staged = vcs.getStagedFiles();
for (String file : staged) {
    System.out.println("Staged: " + file);
}
```

#### Commit Operations

```java
public boolean commit(String message)
```
**Parameters**:
- `message` (String): Commit message (1-500 characters)

**Returns**: `boolean` - true if successful, false otherwise

**Behavior**:
1. Validates message
2. Gets staged files
3. Creates Commit object
4. Generates summary
5. Stores to repository
6. Clears staging area

**Exceptions**:
- Message cannot be null/empty
- Message cannot exceed 500 characters
- Must have staged files

**Example**:
```java
if (vcs.commit("Fixed bug in authentication")) {
    System.out.println("Commit created");
}
```

```java
public String getCommitId(String message)
```
**Parameters**:
- `message` (String): Commit message

**Returns**: `String` - generated commit ID (8 characters), null on error

**Example**:
```java
String id = vcs.getCommitId("Initial commit");
System.out.println("Commit: " + id); // e.g., "a1b2c3d4"
```

#### Query Operations

```java
public List<Commit> getCommitHistory()
```
**Returns**: `List<Commit>` - all commits, empty if none

**Example**:
```java
List<Commit> commits = vcs.getCommitHistory();
System.out.println("Total commits: " + commits.size());
for (Commit c : commits) {
    System.out.println("- " + c.getMessage());
}
```

```java
public Commit getCommit(String commitId)
```
**Parameters**:
- `commitId` (String): 8-character commit ID

**Returns**: `Commit` - commit object, null if not found

**Example**:
```java
Commit commit = vcs.getCommit("a1b2c3d4");
if (commit != null) {
    System.out.println(commit.getMessage());
}
```

#### Diff & Status Operations

```java
public List<String> generateDiff(String commitId1, String commitId2)
```
**Parameters**:
- `commitId1` (String): First commit ID
- `commitId2` (String): Second commit ID

**Returns**: `List<String>` - formatted diff lines

**Behavior**:
1. Retrieves both commits
2. Generates detailed diff
3. Returns formatted output

**Example**:
```java
List<String> diff = vcs.generateDiff("a1b2c3d4", "e5f6g7h8");
for (String line : diff) {
    System.out.println(line);
}
```

```java
public String getRepositoryStatus()
```
**Returns**: `String` - formatted status report

**Example**:
```java
String status = vcs.getRepositoryStatus();
System.out.println(status);
/*
Repository: /home/user/myrepo
Initialized: Yes
Commits: 5
Staged Files: 2
*/
```

#### Getter Methods

```java
public Repository getRepository()
```
**Returns**: `Repository` - current repository object

---

## Repository API

### Repository Class

**Package**: `com.azaala.vcs`

#### Constructor

```java
public Repository(String repositoryPath)
```
**Parameters**:
- `repositoryPath` (String): Full path to repository

**Exceptions**:
- `IllegalArgumentException` - if path is null/empty

**Behavior**:
1. Stores path and derives name
2. Sets creation timestamp
3. Loads existing commits

**Example**:
```java
Repository repo = new Repository("/home/user/myrepo");
System.out.println("Repository: " + repo.getName());
```

#### Initialization Check

```java
public boolean isInitialized()
```
**Returns**: `boolean` - true if directory structure exists

**Checks**:
- `data/` directory exists
- `data/commits/` directory exists
- `data/index/` directory exists

**Example**:
```java
if (repo.isInitialized()) {
    System.out.println("Repository is ready");
} else {
    System.out.println("Repository needs initialization");
}
```

#### Commit Management

```java
public String createCommit(Commit commit)
```
**Parameters**:
- `commit` (Commit): Commit to save

**Returns**: `String` - commit ID if successful, null on error

**Behavior**:
1. Creates commit_<id> directory
2. Saves commit metadata
3. Updates commits.log
4. Adds to commits list

**Example**:
```java
List<String> files = Arrays.asList("file1.java", "file2.java");
Commit commit = new Commit("Initial commit", files);
String id = repo.createCommit(commit);
if (id != null) {
    System.out.println("Commit created: " + id);
}
```

```java
public void loadExistingCommits()
```
**Behavior**:
- Reads commits.log file
- Parses each commit line
- Populates commits list
- Called in constructor

#### Accessors

```java
public String getPath()              // Repository path
public String getName()              // Repository name
public LocalDateTime getCreatedAt()  // Creation timestamp
public List<Commit> getCommits()     // All commits
public Long getRepoId()              // Database ID
public String getRepositoryPath()    // Full path (duplicate)
```

#### Mutators

```java
public void setPath(String repositoryPath)
public void setName(String name)
public void setRepoId(Long repoId)
```

---

## Commit API

### Commit Class

**Package**: `com.azaala.vcs`

#### Constructors

**Basic Constructor** (Most Common)
```java
public Commit(String message, List<String> changedFiles)
```
**Parameters**:
- `message` (String): Commit message (1-500 chars)
- `changedFiles` (List<String>): List of file paths

**Auto-Generated**:
- `commitId` - 8-char UUID
- `timestamp` - current time
- `summary` - default summary

**Exceptions**:
- `IllegalArgumentException` - invalid message or files

**Example**:
```java
List<String> files = Arrays.asList("Main.java", "Utils.java");
Commit commit = new Commit("Added features", files);
System.out.println("ID: " + commit.getCommitId());
```

**Full Constructor** (For Loading)
```java
public Commit(String commitId, String message, 
              LocalDateTime timestamp, List<String> changedFiles)
```
**Parameters**: All explicit, no auto-generation

**Example**:
```java
LocalDateTime timestamp = LocalDateTime.of(2024, 1, 15, 10, 30);
Commit loaded = new Commit(
    "a1b2c3d4",
    "Initial commit",
    timestamp,
    files
);
```

**With Summary**
```java
public Commit(String message, List<String> changedFiles, String summary)
```

#### Accessors

```java
public String getCommitId()           // 8-char ID
public String getMessage()            // Commit message
public String getSummary()            // AI-generated summary
public LocalDateTime getTimestamp()   // Creation time
public List<String> getChangedFiles() // All files
public int getFileCount()             // Number of files
```

#### Mutators

```java
public void setSummary(String summary)
```

#### Validation Methods

```java
private void validateMessage(String message)
// Checks: not null, not empty, <= 500 chars
// Throws: IllegalArgumentException

private void validateChangedFiles(List<String> changedFiles)
// Checks: not null, not empty
// Throws: IllegalArgumentException

private void validateSummary(String summary)
// Checks: not null
// Throws: IllegalArgumentException
```

#### ID Generation

```java
private String generateUniqueId()
// Returns 8-char UUID substring
// Format: "a1b2c3d4"
```

#### Summary Generation

```java
private String generateDefaultSummary(int fileCount)
// Returns: "Modified X file(s)"
```

---

## FileHandler API

### FileHandler Class

**Package**: `com.azaala.vcs`

#### File Reading

```java
public String readFile(String filePath)
```
**Parameters**:
- `filePath` (String): Path to file

**Returns**: `String` - file content, null on error

**Behavior**:
1. Reads entire file
2. Returns as single string
3. Logs IOException

**Example**:
```java
String content = fileHandler.readFile("src/Main.java");
if (content != null) {
    System.out.println("File size: " + content.length());
}
```

#### File Writing

```java
public boolean writeFile(String filePath, String content)
```
**Parameters**:
- `filePath` (String): Path to write
- `content` (String): Content to write

**Returns**: `boolean` - true if successful

**Behavior**:
1. Creates parent directories
2. Overwrites existing file
3. Returns success/failure

**Example**:
```java
boolean success = fileHandler.writeFile(
    "data/output.txt",
    "Hello World"
);
```

#### File Copying

```java
public boolean copyToIndex(String sourceFilePath, String indexPath)
```
**Parameters**:
- `sourceFilePath` (String): Source file path
- `indexPath` (String): Destination directory (staging area)

**Returns**: `boolean` - true if successful

**Example**:
```java
fileHandler.copyToIndex("src/Main.java", "data/index");
```

```java
public boolean copyFile(String sourcePath, String targetPath)
```
**Generic file copy between any locations**

```java
public boolean copyDirectory(String sourcePath, String targetPath)
```
**Recursively copies entire directory**

#### Staging Area Operations

```java
public List<String> getStagedFiles()
```
**Returns**: `List<String>` - paths of staged files

**Example**:
```java
List<String> staged = fileHandler.getStagedFiles();
for (String file : staged) {
    System.out.println("Staged: " + file);
}
```

```java
public void updateStagedFiles(List<String> files)
```
**Overwrites staged files list**

---

## Diff & Summary APIs

### DiffUtil Class

**Package**: `com.azaala.vcs`

#### Detailed Diff Generation

```java
public List<String> generateDetailedDiff(
    Commit commit1, 
    Commit commit2, 
    String repoPath)
```
**Parameters**:
- `commit1` (Commit): First commit
- `commit2` (Commit): Second commit
- `repoPath` (String): Repository path for file access

**Returns**: `List<String>` - formatted diff lines

**Output Format**:
```
╔════════════════════════════════════════════════════════════════════════╗
║                    DETAILED COMMIT COMPARISON                          ║
╚════════════════════════════════════════════════════════════════════════╝

FROM COMMIT: abc12345
  Message: Initial commit
  Date: 2024-01-15 10:30:00

TO COMMIT: def56789
  Message: Fixed bugs
  Date: 2024-01-15 11:45:00

┌────────────────────────────────────────────────────────────────────────┐
│ SUMMARY                                                                │
├────────────────────────────────────────────────────────────────────────┤
│ Files Added:      5                                                    │
│ Files Removed:    2                                                    │
│ Files Modified:   3                                                    │
│ Files Unchanged: 10                                                    │
└────────────────────────────────────────────────────────────────────────┘

───────────────────────────────────────────────────────────────────────────
✨ NEW FILES (5)
───────────────────────────────────────────────────────────────────────────
  ➕ filename.java
  ... more files ...
```

**Example**:
```java
DiffUtil differ = new DiffUtil();
List<Commit> commits = vcs.getCommitHistory();

if (commits.size() >= 2) {
    List<String> diff = differ.generateDetailedDiff(
        commits.get(0),
        commits.get(1),
        "/home/user/myrepo"
    );
    
    for (String line : diff) {
        System.out.println(line);
    }
}
```

### SummaryGenerator Class

**Package**: `com.azaala.vcs`

#### Summary Generation

```java
public String generateSummary(Commit commit, Commit previousCommit)
```
**Parameters**:
- `commit` (Commit): Current commit
- `previousCommit` (Commit): Previous commit (can be null for initial)

**Returns**: `String` - formatted summary

**Output Format**:
```
=== Commit Summary ===
ID: abc12345
Message: Feature: Add authentication
Time: 2024-01-15 10:30:00

Type: Feature commit
Files added: 3 (total: 25)

Breakdown:
 - Code files: 2
 - Docs: 1
 - Config: 0

Changed files (3):
 - src/auth/AuthService.java
 - src/auth/TokenManager.java
 - README.md
```

**Example**:
```java
SummaryGenerator gen = new SummaryGenerator();
Commit current = new Commit("New feature", files);
Commit previous = repo.getCommits().get(repo.getCommits().size() - 1);

String summary = gen.generateSummary(current, previous);
System.out.println(summary);
```

#### Helper Methods

```java
public int countFilesByType(List<String> files, String... extensions)
```
**Example**:
```java
int codeFiles = gen.countFilesByType(files, "java", "py", "cpp");
System.out.println("Code files: " + codeFiles);
```

---

## Database API

### DatabaseManager Class

**Package**: `com.azaala.vcs.persistence`

#### Singleton Access

```java
public static synchronized DatabaseManager getInstance()
```
**Returns**: `DatabaseManager` - singleton instance

**Example**:
```java
DatabaseManager dbManager = DatabaseManager.getInstance();
```

#### Initialization

```java
public void initialize() throws DatabaseException
```
**Throws**: `DatabaseException` - on initialization failure

**Behavior**:
1. Initializes connection pool
2. Tests connection
3. Creates schema if needed

**Example**:
```java
try {
    DatabaseManager.getInstance().initialize();
    System.out.println("Database ready");
} catch (DatabaseException e) {
    System.err.println("DB Error: " + e.getMessage());
}
```

#### Schema Management

```java
public void initializeSchema() throws DatabaseException
```
**Creates database tables and schema**

### ConnectionPool Class

**Package**: `com.azaala.vcs.persistence`

#### Get Connection

```java
public Connection getConnection() throws SQLException
```
**Returns**: `Connection` - database connection from pool

**Usage**:
```java
try (Connection conn = connectionPool.getConnection()) {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM commits");
    // Process results
} catch (SQLException e) {
    e.printStackTrace();
}
```

#### Test Connection

```java
public boolean testConnection()
```
**Returns**: `boolean` - true if connection works

#### Shutdown

```java
public void shutdown()
```
**Closes all pool connections and resources**

### DatabaseException Class

**Package**: `com.azaala.vcs.persistence`

#### Constructors

```java
public DatabaseException(String message)
public DatabaseException(String message, Throwable cause)
```

---

## Async API

### BaseVCSWorker Class

**Package**: `com.azaala.vcs.async`

#### Constructor

```java
public BaseVCSWorker(ProgressListener progressListener)
public BaseVCSWorker()
```

#### Key Methods

```java
protected abstract T doInBackground() throws Exception
```
- Must implement in subclass
- Runs on background thread
- Must not update GUI

```java
protected void publishProgress(String message)
protected void publishProgress(String message, int progress)
```
- Reports progress to EDT
- Triggers onProgress callbacks

```java
protected void onSuccess(T result)
protected void onError(Exception e)
protected void onCancellation()
```
- Override for custom handling

**Example**:
```java
public class CustomWorker extends BaseVCSWorker<String> {
    
    public CustomWorker(ProgressListener listener) {
        super(listener);
    }
    
    @Override
    protected String doInBackground() throws Exception {
        publishProgress("Starting...", 0);
        
        // Long operation
        for (int i = 0; i < 100; i++) {
            Thread.sleep(100);
            publishProgress("Progress: " + i, i);
        }
        
        return "Completed";
    }
    
    @Override
    protected void onSuccess(String result) {
        System.out.println("Success: " + result);
    }
    
    @Override
    protected void onError(Exception e) {
        System.err.println("Error: " + e.getMessage());
    }
}

// Usage
CustomWorker worker = new CustomWorker(progressListener);
worker.execute();
```

### WorkerFactory Class

**Package**: `com.azaala.vcs.async`

#### Factory Methods

```java
public static BaseVCSWorker<?> createRepositoryInitWorker(
    String path, ProgressListener listener)
```

```java
public static BaseVCSWorker<?> createAddFileWorker(
    String filepath, ProgressListener listener)
```

```java
public static BaseVCSWorker<?> createCommitWorker(
    String message, ProgressListener listener)
```

```java
public static BaseVCSWorker<?> createDiffWorker(
    String commitId1, String commitId2, ProgressListener listener)
```

**Example**:
```java
ProgressListener listener = new ProgressListener() {
    @Override
    public void onProgress(String msg, int progress) {
        progressBar.setValue(progress);
    }
    
    @Override
    public void onSuccess(String result) {
        JOptionPane.showMessageDialog(null, result);
    }
    
    @Override
    public void onError(String msg, Throwable error) {
        JOptionPane.showMessageDialog(null, "Error: " + msg);
    }
};

BaseVCSWorker<?> worker = WorkerFactory.createCommitWorker(
    "Initial commit", listener
);
worker.execute();
```

### ProgressListener Interface

**Package**: `com.azaala.vcs.async`

#### Methods

```java
void onProgress(String message, int progress)
```
- Called periodically (0-100 progress)

```java
void onSuccess(String result)
```
- Called on successful completion

```java
void onError(String message, Throwable error)
```
- Called on exception

---

## GUI API

### Dashboard Class

**Package**: `com.azaala.vcs.gui`

#### Constructor

```java
public Dashboard()
```
- Creates main application window
- Initializes database
- Builds all components
- Shows window

#### Methods

```java
public void refresh()
```
- Refreshes all panels with current data

```java
public void setRepository(Repository repo)
```
- Sets current repository

```java
public Repository getRepository()
```
- Gets current repository

### UITheme Class

**Package**: `com.azaala.vcs.gui`

#### Static Methods

```java
public static void applyTheme()
```
- Applies theme to all components
- Sets colors, fonts, look & feel

#### Color Constants

```java
public static final Color PRIMARY = new Color(41, 128, 185);
public static final Color SUCCESS = new Color(46, 204, 113);
public static final Color ERROR = new Color(231, 76, 60);
// ... and more
```

#### UI Constants

```java
public static final int PADDING_SMALL = 5;
public static final int PADDING_MEDIUM = 10;
public static final int BUTTON_HEIGHT = 35;
// ... and more
```

---

## Code Examples

### Complete Workflow

```java
// Initialize
VCS vcs = new VCS();
vcs.initRepository("/home/user/myrepo");

// Add files
vcs.addFile("src/Main.java");
vcs.addFile("src/Utils.java");

// View staged files
List<String> staged = vcs.getStagedFiles();
System.out.println("Staged: " + staged.size() + " files");

// Commit
vcs.commit("Added main functionality");

// View history
List<Commit> commits = vcs.getCommitHistory();
for (Commit c : commits) {
    System.out.println(c.getMessage() + " (" + c.getCommitId() + ")");
}

// Generate diff
if (commits.size() >= 2) {
    List<String> diff = vcs.generateDiff(
        commits.get(0).getCommitId(),
        commits.get(1).getCommitId()
    );
    for (String line : diff) {
        System.out.println(line);
    }
}
```

### Async Operations

```java
// Initialize database
try {
    DatabaseManager.getInstance().initialize();
} catch (DatabaseException e) {
    System.err.println("Database error: " + e.getMessage());
}

// Create progress listener
ProgressListener listener = new ProgressListener() {
    @Override
    public void onProgress(String message, int progress) {
        System.out.println("[" + progress + "%] " + message);
    }
    
    @Override
    public void onSuccess(String result) {
        System.out.println("✓ Success: " + result);
    }
    
    @Override
    public void onError(String message, Throwable error) {
        System.err.println("✗ Error: " + message);
        error.printStackTrace();
    }
};

// Execute async operation
BaseVCSWorker<?> worker = WorkerFactory.createCommitWorker(
    "My commit", listener
);
worker.execute();
```

---

## Error Codes

### Common Exceptions

| Exception | Cause | Solution |
|-----------|-------|----------|
| `IllegalArgumentException` | Invalid input parameter | Check parameter validity |
| `IOException` | File I/O error | Check file path and permissions |
| `SQLException` | Database error | Check database connection |
| `DatabaseException` | Custom DB error | Check database configuration |
| `SecurityException` | Security violation | Check file/directory permissions |

### Error Messages

| Message | Cause | Action |
|---------|-------|--------|
| "Repository path cannot be empty" | Null/empty path | Provide valid path |
| "Repository not initialized" | Missing data/ dirs | Call initRepository() |
| "Commit message cannot be empty" | Empty message | Provide message |
| "No files in commit" | Empty file list | Stage files first |
| "Failed to connect to database" | DB unreachable | Check MySQL service |
| "Invalid repository path" | Path contains dangerous chars | Validate path |

---

This API reference provides comprehensive documentation for all public interfaces in Azaala VCS.

