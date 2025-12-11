# 🏗️ Azaala VCS - Component Documentation

Complete technical documentation of all components in the Azaala VCS system.

---

## Table of Contents

1. [Core Components](#core-components)
2. [GUI Components](#gui-components)
3. [Async & Threading Components](#async--threading-components)
4. [Persistence Components](#persistence-components)
5. [Component Dependencies](#component-dependencies)
6. [Data Flow](#data-flow)

---

## Core Components

### 1. Main.java - Application Entry Point

**Location**: `com.azaala.vcs.Main`

**Purpose**: Entry point for the application. Handles both GUI and console mode launching.

#### Key Methods

```java
public static void main(String[] args)
```
- Parses command-line arguments
- Launches GUI in Swing Event Dispatch Thread or console mode
- Handles graceful fallback to console if GUI fails

```java
private static void runConsoleMode(String[] args)
```
- Runs the interactive menu or executes direct commands
- Handles scanner resource management

```java
private static void executeCommand(String[] args)
```
- Routes console commands to appropriate handlers
- Supports: init, add, commit, log, diff, status, help, version, exit

#### Usage Examples

```bash
# GUI Mode (Default)
java -cp target/classes com.azaala.vcs.Main

# Console Mode
java -cp target/classes com.azaala.vcs.Main -c

# Direct Command
java -cp target/classes com.azaala.vcs.Main -c init /path/to/repo

# Help
java -cp target/classes com.azaala.vcs.Main -h

# Version
java -cp target/classes com.azaala.vcs.Main -v
```

---

### 2. VCS.java - Core Facade

**Location**: `com.azaala.vcs.VCS`

**Purpose**: Main facade providing unified interface to all VCS operations. Acts as the "control center" coordinating all subsystems.

#### Key Properties

```java
private Repository repository;           // Current repository
private FileHandler fileHandler;         // File operations
private DiffUtil diffUtil;              // Diff generation
private SummaryGenerator summaryGenerator; // Summary creation
```

#### Key Methods

**Repository Operations**
```java
public boolean initRepository(String path)
```
- Initializes a new repository at specified path
- Creates required directory structure
- Validates path security
- Returns true if successful

**File Operations**
```java
public boolean addFile(String filePath)
public boolean addAllFiles(String directoryPath)
public List<String> getStagedFiles()
public void clearStagingArea()
```
- Stages files for commit
- Batch operations supported
- Manages staging area

**Commit Operations**
```java
public boolean commit(String message)
public String getCommitId(String message)
public List<Commit> getCommitHistory()
public Commit getCommit(String commitId)
```
- Creates commits with auto-generated IDs
- Retrieves commit history
- Generates summaries automatically

**Status & Diff Operations**
```java
public String getRepositoryStatus()
public List<String> generateDiff(String commitId1, String commitId2)
```
- Returns current repository status
- Generates detailed diffs between commits

#### Error Handling

All methods use try-catch blocks with proper logging:
```java
try {
    // Operation
} catch (SecurityException e) {
    System.err.println("Security error: " + e.getMessage());
    return false;
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
    return false;
}
```

#### Example Usage

```java
VCS vcs = new VCS();

// Initialize repository
if (vcs.initRepository("/path/to/repo")) {
    System.out.println("Repository initialized");
    
    // Add file
    if (vcs.addFile("src/Main.java")) {
        System.out.println("File staged");
        
        // Commit
        if (vcs.commit("Initial commit")) {
            System.out.println("Commit successful");
            
            // View history
            List<Commit> commits = vcs.getCommitHistory();
            for (Commit c : commits) {
                System.out.println(c.getMessage());
            }
        }
    }
}
```

---

### 3. Repository.java - Repository Management

**Location**: `com.azaala.vcs.Repository`

**Purpose**: Represents a version control repository. Manages commit storage and repository state.

#### Key Properties

```java
private String repositoryPath;          // Full path to repository
private String name;                    // Repository name
private LocalDateTime createdAt;        // Creation timestamp
private List<Commit> commits;           // All commits
private Long repoId;                    // Database ID
```

#### Directory Structure

```
repository_path/
├── data/
│   ├── commits/                    # Stores commits
│   │   └── commit_<id>/           # Individual commit folder
│   │       ├── <filename1>        # Files in commit
│   │       ├── <filename2>
│   │       └── metadata.txt       # Commit metadata
│   ├── index/                     # Staging area
│   │   ├── staged_files.txt      # List of staged files
│   │   └── <staged_file>
│   └── commits.log                # All commits log
└── config.txt                      # Repository configuration
```

#### Key Methods

**Initialization & Status**
```java
public boolean isInitialized()
```
- Checks if required directory structure exists
- Validates `data/`, `data/commits/`, `data/index/` directories

**Commit Management**
```java
public String createCommit(Commit commit)
```
- Saves commit to file system
- Creates commit_<id> directory
- Stores metadata in JSON
- Updates commits.log
- Returns commit ID or null on failure

```java
public void loadExistingCommits()
```
- Loads all commits from commits.log on initialization
- Parses commit metadata
- Populates commits list

**Accessors**
```java
public String getPath()
public String getName()
public LocalDateTime getCreatedAt()
public List<Commit> getCommits()
public Long getRepoId()
public void setRepoId(Long repoId)
```

#### File Format Examples

**commits.log** - CSV format
```
commit_abc123|Initial commit|2024-01-15 10:30:00
commit_def456|Fix bug|2024-01-15 11:45:00
```

**metadata.txt** - Text format
```
ID: commit_abc123
Message: Initial commit
Timestamp: 2024-01-15 10:30:00
Files: 3
Summary: Added core files
```

#### Example Usage

```java
Repository repo = new Repository("/home/user/myrepo");

if (!repo.isInitialized()) {
    System.out.println("Repository not initialized");
    return;
}

// Get all commits
List<Commit> commits = repo.getCommits();
System.out.println("Total commits: " + commits.size());

// Create new commit
Commit commit = new Commit("New feature", files);
String commitId = repo.createCommit(commit);
System.out.println("Created commit: " + commitId);
```

---

### 4. Commit.java - Commit Model

**Location**: `com.azaala.vcs.Commit`

**Purpose**: Represents a single commit snapshot in the version control system.

#### Key Properties

```java
private String commitId;                // Unique identifier
private String message;                 // Commit message (max 500 chars)
private String summary;                 // AI-generated summary
private LocalDateTime timestamp;        // Creation time
private List<String> changedFiles;      // Files in commit
```

#### Constructors

**Basic Constructor**
```java
public Commit(String message, List<String> changedFiles)
```
- Auto-generates commitId using UUID
- Sets timestamp to now
- Generates default summary

**Full Constructor**
```java
public Commit(String commitId, String message, 
              LocalDateTime timestamp, List<String> changedFiles)
```
- Used for loading commits from storage
- All parameters must be valid

**With Summary**
```java
public Commit(String message, List<String> changedFiles, String summary)
```
- Allows custom summary specification

#### Key Methods

**Getters**
```java
public String getCommitId()
public String getMessage()
public LocalDateTime getTimestamp()
public List<String> getChangedFiles()
public String getSummary()
public int getFileCount()
```

**Setters**
```java
public void setSummary(String summary)
```

**Summary Generation**
```java
private String generateDefaultSummary(int fileCount)
```
- Auto-generates format: "Modified {count} file(s)"
- Called in constructors

**ID Generation**
```java
private String generateUniqueId()
```
- Uses UUID.randomUUID().substring(0, 8)
- Creates 8-character unique ID

#### Validation

All constructors validate input:

```java
private void validateMessage(String message)
```
- Checks if null or empty
- Validates length (max 500 characters)
- Throws IllegalArgumentException

```java
private void validateChangedFiles(List<String> changedFiles)
```
- Ensures list is not null
- Checks if list is empty

```java
private void validateSummary(String summary)
```
- Ensures summary is not null
- Validates length constraints

#### Example Usage

```java
// Create commit with auto-generated ID
List<String> files = Arrays.asList("src/Main.java", "src/Utils.java");
Commit commit = new Commit("Initial commit", files);
System.out.println("Commit ID: " + commit.getCommitId());

// With custom summary
String summary = "Added core functionality and utilities";
Commit commit2 = new Commit("Feature", files, summary);

// Load from storage
LocalDateTime timestamp = LocalDateTime.now();
Commit loaded = new Commit("abc12345", "Fix bug", timestamp, files);
```

---

### 5. FileHandler.java - File Operations

**Location**: `com.azaala.vcs.FileHandler`

**Purpose**: Centralized file I/O operations for the VCS system.

#### Key Methods

**Read Operations**
```java
public String readFile(String filePath)
```
- Reads entire file content using Files.readString()
- Returns null on error
- Handles IOException gracefully

**Write Operations**
```java
public boolean writeFile(String filePath, String content)
```
- Creates parent directories if needed
- Overwrites existing files
- Returns true on success

**File Copying**
```java
public boolean copyToIndex(String sourceFilePath, String indexPath)
```
- Copies file to staging area
- Creates index directory if needed
- Extracts filename for target path

```java
public boolean copyFile(String sourcePath, String targetPath)
```
- General purpose file copy
- Handles all error cases

```java
public boolean copyDirectory(String sourcePath, String targetPath)
```
- Recursively copies entire directory
- Creates target directory structure
- Handles symbolic links

**Staging Area Operations**
```java
public List<String> getStagedFiles()
```
- Reads staged_files.txt
- Returns list of file paths

```java
public void updateStagedFiles(List<String> files)
```
- Writes staged files list to index
- Overwrites previous staging

**Metadata Operations**
```java
public boolean writeCommitMetadata(String commitId, String metadata)
public String readCommitMetadata(String commitId)
```
- Stores/retrieves commit metadata
- Uses JSON format

#### Error Handling

All methods include try-catch with logging:
```java
try {
    // File operation
} catch (IOException e) {
    System.err.println("Error reading/writing file: " + e.getMessage());
    return null; // or false
}
```

#### Constants

```java
private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
```

#### Example Usage

```java
FileHandler handler = new FileHandler();

// Read file
String content = handler.readFile("src/Main.java");
if (content != null) {
    System.out.println("File size: " + content.length());
}

// Write file
boolean success = handler.writeFile("data/output.txt", "Some content");

// Stage file
handler.copyToIndex("src/Main.java", "data/index");

// Get staged files
List<String> staged = handler.getStagedFiles();
System.out.println("Staged: " + staged.size() + " files");
```

---

### 6. DiffUtil.java - Diff Generation

**Location**: `com.azaala.vcs.DiffUtil`

**Purpose**: Advanced diff analysis and comparison between commits with detailed reporting.

#### Key Methods

**Main Diff Generation**
```java
public List<String> generateDetailedDiff(Commit commit1, Commit commit2, 
                                         String repoPath)
```
- Compares two commits comprehensively
- Categorizes files: added, removed, modified
- Returns formatted diff lines
- Includes statistics and visual formatting

#### Categories

The diff categorizes files into:

1. **Added Files** - Files only in commit2
   ```
   ✨ NEW FILES (n)
     ➕ filename.ext
   ```

2. **Removed Files** - Files only in commit1
   ```
   🗑️  DELETED FILES (n)
     ➖ filename.ext
   ```

3. **Modified Files** - Files in both commits
   ```
   📝 MODIFIED FILES (n)
     ✏️  filename.ext
   ```

#### Output Format

The diff output uses beautiful formatting:

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
```

#### Helper Methods

```java
public List<String> readFileContents(String filePath)
```
- Reads file line by line
- Returns list of lines

```java
public String compareFiles(String file1, String file2)
```
- Performs line-by-line comparison
- Uses diff algorithm

#### File Content Extraction

The diff also shows file contents:
```
  File Content:
  1: public class Main {
  2:     public static void main(String[] args) {
  3:         System.out.println("Hello");
  4:     }
  5: }
```

#### Example Usage

```java
DiffUtil differ = new DiffUtil();

Commit commit1 = repo.getCommits().get(0);
Commit commit2 = repo.getCommits().get(1);

List<String> diff = differ.generateDetailedDiff(
    commit1, commit2, "/path/to/repo"
);

for (String line : diff) {
    System.out.println(line);
}
```

---

### 7. SummaryGenerator.java - Summary Generation

**Location**: `com.azaala.vcs.SummaryGenerator`

**Purpose**: Intelligent analysis of commits to generate human-readable summaries.

#### Key Methods

**Summary Generation**
```java
public String generateSummary(Commit commit, Commit previousCommit)
```
- Analyzes current and previous commits
- Generates detailed summary text
- If previousCommit is null, treats as initial commit
- Returns formatted summary string

#### Summary Components

The generated summary includes:

1. **Commit Metadata**
   ```
   === Commit Summary ===
   ID: abc12345
   Message: Added new features
   Time: 2024-01-15 10:30:00
   ```

2. **File Count Analysis**
   ```
   Type: Feature commit
   Files added: 5 (total: 25)
   Files removed: 2 (total: 23)
   Files modified: 3 (no count change)
   ```

3. **File Type Breakdown**
   ```
   Breakdown:
    - Code files: 4
    - Docs: 1
    - Config: 2
   ```

4. **Changed Files List**
   ```
   Changed files (7):
    - src/Main.java
    - src/Utils.java
    - ... (limited to 20)
   ```

#### Pattern-Based Analysis

```java
private static final Pattern FUNCTION_PATTERN = Pattern.compile(
    "(public|private|protected)?\\s+\\w+\\s+\\w+\\(.*\\)\\s*\\{?"
);
```
- Detects new function definitions
- Supports Java method declarations

```java
private static final Pattern TODO_PATTERN = Pattern.compile(
    "(TODO|FIXME):\\s*(.*)"
);
```
- Finds TODO/FIXME comments

#### Helper Methods

```java
public int countFilesByType(List<String> files, String... extensions)
```
- Counts files by extension
- Example: `countFilesByType(files, "java", "py", "cpp")`

```java
private String detectFileType(String filename)
```
- Returns: "code", "docs", "config", or "other"
- Based on file extension

```java
private List<String> extractTODOs(String content)
```
- Finds all TODO/FIXME comments
- Returns list of comment text

#### Example Output

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

#### Example Usage

```java
SummaryGenerator generator = new SummaryGenerator();

Commit current = new Commit("New feature", files);
Commit previous = repo.getCommits().get(repo.getCommits().size() - 1);

String summary = generator.generateSummary(current, previous);
System.out.println(summary);

// Update commit with summary
current.setSummary(summary);
```

---

### 8. CommandHandler.java - CLI Command Router

**Location**: `com.azaala.vcs.CommandHandler`

**Purpose**: Parses and routes command-line commands to appropriate handlers.

#### Supported Commands

```java
private static final List<String> VALID_COMMANDS = Arrays.asList(
    "init", "add", "commit", "status", "log", "diff", "help", "version", "exit"
);
```

#### Command Methods

**Init**
```java
private boolean handleInit(String[] args)
```
- Usage: `init [path]`
- Initializes repository at path
- Interactive prompt if no path provided

**Add**
```java
private boolean handleAdd(String[] args)
```
- Usage: `add <filepath> [<filepath2> ...]`
- Stages files for commit
- Multiple files supported

**Commit**
```java
private boolean handleCommit(String[] args)
```
- Usage: `commit <message>`
- Creates commit with message
- Auto-generates summary

**Status**
```java
private boolean handleStatus(String[] args)
```
- Usage: `status`
- Shows current repository status
- Lists staged/unstaged files

**Log**
```java
private boolean handleLog(String[] args)
```
- Usage: `log [--limit N]`
- Displays commit history
- Optional limit parameter

**Diff**
```java
private boolean handleDiff(String[] args)
```
- Usage: `diff <commit1_id> <commit2_id>`
- Compares two commits
- Shows detailed diff

**Help**
```java
private boolean handleHelp(String[] args)
```
- Usage: `help [command]`
- Shows help for command or all commands

**Version**
```java
private boolean handleVersion(String[] args)
```
- Usage: `version`
- Displays application version

**Exit**
```java
private boolean handleExit(String[] args)
```
- Usage: `exit`
- Cleanly exits application

#### Error Handling

```java
public boolean executeCommand(String command, String[] args)
```
- Validates command is not empty
- Checks command is in VALID_COMMANDS
- Handles all exceptions
- Returns false on error

#### Example Usage

```java
CommandHandler handler = new CommandHandler();

// Execute command
if (handler.executeCommand("init", new String[]{"/path/to/repo"})) {
    System.out.println("Repository initialized");
}

if (handler.executeCommand("add", new String[]{"src/Main.java"})) {
    System.out.println("File staged");
}

if (handler.executeCommand("commit", new String[]{"Initial commit"})) {
    System.out.println("Commit created");
}
```

---

### 9. Utils.java - Utility Functions

**Location**: `com.azaala.vcs.Utils`

**Purpose**: Common utility functions for the VCS system.

#### Security Functions

**Path Validation**
```java
public static boolean isValidPath(String path)
```
- Checks path is not null/empty
- Detects dangerous patterns: `..`, `//`, absolute paths on Windows
- Uses VALID_PATH_PATTERN regex
- Returns true if valid

```java
private static final Pattern VALID_PATH_PATTERN = 
    Pattern.compile("^[a-zA-Z0-9._/\\\\:\\-\\s]+$");
```

**File Boundary Checking**
```java
public static boolean isFileWithinDirectory(File file, File directory)
```
- Ensures file is within directory boundary
- Prevents directory traversal attacks
- Uses canonical paths
- Returns true if file is within directory

#### File/Path Functions

**Get Filename**
```java
public static String getFileName(String filePath)
```
- Extracts filename from path
- Handles null/empty paths
- Example: `/home/user/file.java` → `file.java`

**Get File Extension**
```java
public static String getFileExtension(String filename)
```
- Returns file extension
- Handles multiple dots
- Example: `Main.java` → `java`

**Normalize Path**
```java
public static String normalizePath(String path)
```
- Converts backslashes to forward slashes
- Removes trailing slashes
- Removes double slashes
- Returns normalized path

#### Identification Functions

**Generate Unique ID**
```java
public static String generateUniqueId()
```
- Uses UUID.randomUUID()
- Returns first 8 characters
- Example: `a1b2c3d4`

**Calculate File Hash**
```java
public static String calculateFileHash(String filePath)
```
- Computes SHA-256 hash
- Uses MessageDigest
- Returns hex string
- Null if error

```java
public static String bytesToHex(byte[] bytes)
```
- Converts bytes to hex string
- Example: [10, 20, 30] → `0a141e`

#### Timestamp Functions

**Format Timestamp**
```java
public static String formatTimestamp(LocalDateTime dateTime)
```
- Format: `yyyy-MM-dd HH:mm:ss`
- Handles null gracefully
- Returns formatted string

**Parse Timestamp**
```java
public static LocalDateTime parseTimestamp(String dateTimeStr)
```
- Parses formatted timestamp
- Returns LocalDateTime or null

#### Example Usage

```java
// Security check
if (Utils.isValidPath(userInput)) {
    // Use path safely
}

// File operations
String filename = Utils.getFileName("/home/user/file.java"); // "file.java"
String ext = Utils.getFileExtension("document.pdf"); // "pdf"

// Identification
String id = Utils.generateUniqueId(); // "a1b2c3d4"
String hash = Utils.calculateFileHash("src/Main.java");

// Timestamp
String formatted = Utils.formatTimestamp(LocalDateTime.now());
LocalDateTime parsed = Utils.parseTimestamp("2024-01-15 10:30:00");
```

---

## GUI Components

### 10. Dashboard.java - Main Application Window

**Location**: `com.azaala.vcs.gui.Dashboard`
**Extends**: `JFrame`

**Purpose**: Main application window providing tabbed interface with all GUI components.

#### Key Components

**Menu Bar**
```java
private JMenuBar menuBar;
```
- File menu: New, Open, Exit
- Edit menu: Preferences
- Repository menu: Init, Status
- Help menu: About, Help

**Toolbar**
```java
private JToolBar toolBar;
```
- Quick buttons: Init, Add, Commit, Diff, Refresh
- Status indicators
- Search field

**Tabbed Pane**
```java
private JTabbedPane tabbedPane;
```
- Overview Panel
- History Panel
- Status Panel
- Diff Panel
- Settings Panel

**Status Bar**
```java
private JPanel statusBar;
```
- Current repository path
- Operation status
- Progress indicator

#### Key Methods

**Initialization**
```java
public Dashboard()
```
- Applies theme
- Initializes database
- Builds all components
- Shows frame

```java
private void initializeFrame()
```
- Sets window title
- Sets default close operation
- Sets window size and position
- Creates main layout

**Building Components**
```java
private void buildMenuBar()
private void buildToolBar()
private void buildTabbedPane()
private void buildStatusBar()
```

**Event Handling**
```java
private void setupListeners()
```
- Button click listeners
- Menu item listeners
- Window close handler

#### Event Handlers

```java
private void onInitRepository()
private void onAddFile()
private void onAddAllFiles()
private void onCommit()
private void onRefresh()
private void onDiff()
private void onPreferences()
```

#### Example UI Layout

```
┌──────────────────────────────────────────────────────────────┐
│ File  Edit  Repository  Help                                 │
├──────────────────────────────────────────────────────────────┤
│ [Init] [Add] [Commit] [Diff] [Refresh]                 [?]   │
├──────────────────────────────────────────────────────────────┤
│ ┌─────────────┬──────────┬────────┬──────────┬──────────┐   │
│ │ Overview  │ History │ Status │ Diff  │ Settings │   │
│ ├───────────────────────────────────────────────────────┤   │
│ │                                                       │   │
│ │  [Panel Content]                                     │   │
│ │                                                       │   │
│ ├───────────────────────────────────────────────────────┤   │
│ │ Repository: /home/user/repo | Status: Ready         │   │
│ └───────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────┘
```

---

### 11. OverviewPanel.java - Repository Overview

**Location**: `com.azaala.vcs.gui.OverviewPanel`
**Extends**: `JPanel`

**Purpose**: Displays overview of current repository status and statistics.

#### Key Sections

**Repository Info**
- Repository name
- Repository path
- Creation date
- Last commit date

**Statistics**
- Total commits
- Total files
- Staged files count
- Repository size

**Latest Commits**
- List of last 5 commits
- Commit ID, message, timestamp
- Click to view details

**Quick Actions**
- Initialize repository button
- Open repository button
- Repository settings button

---

### 12. HistoryPanel.java - Commit History

**Location**: `com.azaala.vcs.gui.HistoryPanel`
**Extends**: `JPanel`

**Purpose**: Displays and navigates commit history with filtering and search.

#### Features

**Commit List**
```java
private JList<String> commitList;
```
- Displays all commits
- Shows commit ID and message
- Double-click to view details

**Search & Filter**
```java
private JTextField searchField;
```
- Search by commit message
- Filter by date range
- Filter by file changes

**Commit Details**
```java
private JTextArea detailsArea;
```
- Full commit information
- Changed files list
- Summary text
- Timestamp and author

**File List**
```java
private JList<String> filesList;
```
- Files modified in commit
- File statistics
- View file diff

---

### 13. StatusPanel.java - Repository Status

**Location**: `com.azaala.vcs.gui.StatusPanel`
**Extends**: `JPanel`

**Purpose**: Shows current repository status with staged and unstaged files.

#### Layout

```
┌─────────────────────────────────────────┐
│ REPOSITORY STATUS                       │
├─────────────────────────────────────────┤
│ Staged Files (n)        Unstaged Files  │
│ ┌───────────────┐      ┌──────────────┐│
│ │ • file1.java │      │• file5.java  ││
│ │ • file2.java │      │• file6.java  ││
│ │ • file3.txt  │      └──────────────┘│
│ └───────────────┘                      │
│  [Unstage] [Diff]      [Stage] [View] │
└─────────────────────────────────────────┘
```

#### Key Methods

```java
void updateStatus()
```
- Loads current status from VCS
- Refreshes file lists
- Updates statistics

```java
void stageFile(String filepath)
void unstageFile(String filepath)
```
- Moves files between staged/unstaged
- Updates UI

```java
void viewFileDiff(String filepath)
```
- Shows diff for file
- Opens diff viewer

---

### 14. DiffPanel.java - Diff Viewer

**Location**: `com.azaala.vcs.gui.DiffPanel`
**Extends**: `JPanel`

**Purpose**: Compares two commits and displays detailed diff results.

#### Components

**Commit Selection**
```java
private JComboBox<String> commit1Box;
private JComboBox<String> commit2Box;
private JButton diffButton;
```

**Diff Display**
```java
private JTextArea diffArea;
```
- Shows full diff output
- Syntax highlighting
- Scrollable

**Statistics**
```java
private JLabel statsLabel;
```
- Files added/removed/modified
- Line count changes

#### Workflow

1. Select first commit from dropdown
2. Select second commit from dropdown
3. Click "Compare" button
4. View detailed diff results
5. Optional: Export diff to file

---

### 15. SettingsPanel.java - Settings Management

**Location**: `com.azaala.vcs.gui.SettingsPanel`
**Extends**: `JPanel`

**Purpose**: Configure application and database settings.

#### Settings Categories

**Database Settings**
- Host/Port
- Database name
- Username/Password
- Connection pool size

**Repository Settings**
- Default repository path
- Auto-init repositories
- Default commit template

**UI Settings**
- Theme selection
- Font size
- Color scheme

**Advanced Settings**
- Max file size
- Auto-save interval
- Compression options

---

### 16. PreferencesDialog.java - User Preferences

**Location**: `com.azaala.vcs.gui.PreferencesDialog`
**Extends**: `JDialog`

**Purpose**: Dialog for managing user preferences and settings.

#### Preference Categories

**Appearance**
- Theme selection (Light/Dark)
- Font family and size
- Window size on startup

**Database**
- Connection string
- Credentials
- Connection pool settings

**Behavior**
- Auto-initialize repositories
- Auto-save preferences
- Show confirmation dialogs
- Commit message templates

#### Methods

```java
public int showDialog(Component parent)
```
- Shows modal dialog
- Returns OK/CANCEL

```java
private void savePreferences()
private void loadPreferences()
```
- Persist/load preferences from file

---

### 17. UITheme.java - Theme Management

**Location**: `com.azaala.vcs.gui.UITheme`

**Purpose**: Centralized theme and UI constants management.

#### Color Palette

```java
// Primary Colors
public static final Color PRIMARY = new Color(41, 128, 185);
public static final Color SECONDARY = new Color(52, 152, 219);

// Text Colors
public static final Color TEXT_PRIMARY = new Color(44, 62, 80);
public static final Color TEXT_SECONDARY = new Color(127, 140, 141);

// UI Colors
public static final Color BACKGROUND = new Color(236, 240, 241);
public static final Color PANEL_BACKGROUND = new Color(255, 255, 255);
public static final Color BORDER = new Color(189, 195, 199);

// Status Colors
public static final Color SUCCESS = new Color(46, 204, 113);
public static final Color WARNING = new Color(241, 196, 15);
public static final Color ERROR = new Color(231, 76, 60);
```

#### UI Constants

```java
public static final int PADDING_SMALL = 5;
public static final int PADDING_MEDIUM = 10;
public static final int PADDING_LARGE = 15;

public static final int BUTTON_HEIGHT = 35;
public static final int BUTTON_WIDTH = 100;
```

#### Font Settings

```java
public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 12);
public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 12);
public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
public static final Font FONT_MONOSPACE = new Font("Courier New", Font.PLAIN, 11);
```

#### Methods

```java
public static void applyTheme()
```
- Applies theme to all components
- Sets look and feel
- Configures colors and fonts

---

## Async & Threading Components

### 18. BaseVCSWorker.java - Abstract Worker Base

**Location**: `com.azaala.vcs.async.BaseVCSWorker`
**Extends**: `SwingWorker<T, String>`

**Purpose**: Abstract base class for all asynchronous VCS operations.

#### Type Parameters

```java
<T> - Return type of background computation
```

#### Key Methods

**Background Work**
```java
protected abstract T doInBackground() throws Exception
```
- Must be implemented by subclasses
- Runs on background thread
- Must not update UI

**Progress Reporting**
```java
protected void publishProgress(String message)
protected void publishProgress(String message, int progress)
```
- Publishes progress to EDT
- Triggers process() callback

**Completion**
```java
@Override
protected void done()
```
- Called when work completes
- Gets result with get()
- Calls onSuccess() or onError()

**Result Processing**
```java
protected void process(List<String> chunks)
```
- Called on EDT when process() is published
- Updates progress UI

#### Callback Methods

```java
protected void onSuccess(T result)
protected void onError(Exception e)
protected void onCancellation()
```
- Override for custom handling
- Called in done() method

#### Progress Notification

```java
protected void notifyProgress(String message, int progress)
protected void notifySuccess(String result)
protected void notifyError(String message, Throwable error)
```
- Calls progressListener callbacks
- Handles null listener gracefully

#### Example Implementation

```java
public class CustomWorker extends BaseVCSWorker<String> {
    
    public CustomWorker(ProgressListener listener) {
        super(listener);
    }
    
    @Override
    protected String doInBackground() throws Exception {
        // Long operation
        for (int i = 0; i < 100; i++) {
            if (isCancelled()) break;
            
            // Do work
            Thread.sleep(100);
            
            // Report progress
            publishProgress("Processing " + i, i);
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
```

---

### 19. Worker Implementations

**RepositoryInitWorker.java**
- Initializes repository asynchronously
- Reports progress as directories created
- Handles database operations

**AddFileWorker.java**
- Stages single file asynchronously
- Verifies file exists
- Updates staging area
- Reports completion

**AddAllFilesWorker.java**
- Stages multiple files
- Shows progress for each file
- Batch operation support

**CommitWorker.java**
- Creates commit asynchronously
- Generates summary
- Updates database
- Returns commit ID

**DiffWorker.java**
- Generates diff asynchronously
- Large diff handling
- Reports progress

**HistoryLoadWorker.java**
- Loads commit history
- Populates history list
- Handles large histories

**StatusLoadWorker.java**
- Computes repository status
- Counts staged/unstaged files
- Updates status display

---

### 20. WorkerFactory.java - Worker Factory

**Location**: `com.azaala.vcs.async.WorkerFactory`

**Purpose**: Factory pattern implementation for creating worker instances.

#### Factory Methods

```java
public static BaseVCSWorker<?> createRepositoryInitWorker(
    String path, ProgressListener listener)
public static BaseVCSWorker<?> createAddFileWorker(
    String filepath, ProgressListener listener)
public static BaseVCSWorker<?> createCommitWorker(
    String message, ProgressListener listener)
// ... and more
```

#### Advantages

- Centralized worker creation
- Easy to switch implementations
- Consistent initialization
- Dependency injection

#### Example Usage

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

BaseVCSWorker<?> worker = WorkerFactory.createAddFileWorker(
    "src/Main.java", listener
);
worker.execute();
```

---

### 21. ProgressListener.java - Progress Callback Interface

**Location**: `com.azaala.vcs.async.ProgressListener`

**Purpose**: Interface for receiving progress updates from async operations.

#### Methods

```java
void onProgress(String message, int progress)
```
- Called periodically during work
- message: Current status message
- progress: 0-100 percentage

```java
void onSuccess(String result)
```
- Called when work completes successfully
- result: Operation result/message

```java
void onError(String message, Throwable error)
```
- Called on error
- message: Error description
- error: Throwable that occurred

#### Implementation Example

```java
class UIProgressListener implements ProgressListener {
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    @Override
    public void onProgress(String message, int progress) {
        statusLabel.setText(message);
        progressBar.setValue(progress);
    }
    
    @Override
    public void onSuccess(String result) {
        statusLabel.setText("✓ " + result);
        progressBar.setValue(100);
    }
    
    @Override
    public void onError(String message, Throwable error) {
        statusLabel.setText("✗ " + message);
        error.printStackTrace();
    }
}
```

---

## Persistence Components

### 22. DatabaseManager.java - Database Management

**Location**: `com.azaala.vcs.persistence.DatabaseManager`

**Purpose**: Manages database initialization, schema creation, and connection setup.

#### Singleton Pattern

```java
private static DatabaseManager instance;

public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
        instance = new DatabaseManager();
    }
    return instance;
}
```

#### Key Methods

**Initialization**
```java
public void initialize() throws DatabaseException
```
- Initializes connection pool
- Tests database connection
- Creates schema if needed
- Throws DatabaseException on failure

**Schema Management**
```java
public void initializeSchema() throws DatabaseException
```
- Reads schema.sql file
- Executes SQL statements
- Handles existing tables gracefully
- Reports execution results

#### Schema Creation

```sql
-- Repositories
CREATE TABLE repositories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  path VARCHAR(1024) NOT NULL UNIQUE,
  created_at TIMESTAMP,
  description TEXT
);

-- Commits
CREATE TABLE commits (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  commit_id VARCHAR(64) NOT NULL UNIQUE,
  message VARCHAR(500) NOT NULL,
  summary TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY (repo_id) REFERENCES repositories(id)
);

-- Commit Files
CREATE TABLE commit_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  commit_id BIGINT NOT NULL,
  file_path VARCHAR(1024) NOT NULL,
  file_hash VARCHAR(64),
  FOREIGN KEY (commit_id) REFERENCES commits(id)
);

-- Settings
CREATE TABLE settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  key_name VARCHAR(255) NOT NULL,
  key_value TEXT,
  FOREIGN KEY (repo_id) REFERENCES repositories(id)
);
```

#### Example Usage

```java
try {
    DatabaseManager dbManager = DatabaseManager.getInstance();
    dbManager.initialize();
    System.out.println("Database ready");
} catch (DatabaseException e) {
    System.err.println("DB Error: " + e.getMessage());
}
```

---

### 23. DatabaseConfig.java - Configuration

**Location**: `com.azaala.vcs.persistence.DatabaseConfig`

**Purpose**: Manages database configuration from properties file.

#### Configuration Properties

```properties
# MySQL Connection
db.host=localhost
db.port=3306
db.name=azaala_vcs
db.user=root
db.password=your_password

# Connection Pool
db.pool.size=10
db.pool.timeout=30000

# Auto-initialization
db.auto.init=true
```

#### Key Methods

```java
public String getHost()
public int getPort()
public String getDatabaseName()
public String getUsername()
public String getPassword()

public int getPoolSize()
public long getPoolTimeout()
public boolean isAutoInitEnabled()
```

#### Singleton Instance

```java
public static synchronized DatabaseConfig getInstance()
```

---

### 24. ConnectionPool.java - Connection Pooling

**Location**: `com.azaala.vcs.persistence.ConnectionPool`

**Purpose**: HikariCP-based connection pool for efficient database access.

#### Key Features

- **Connection Reuse**: Maintains pool of reusable connections
- **Thread-Safe**: Safe for multi-threaded access
- **Auto-Cleanup**: Automatic connection cleanup
- **Validation**: Periodic connection validation
- **Timeout**: Configurable connection timeout

#### Key Methods

```java
public void initialize() throws DatabaseException
```
- Creates HikariCP connection pool
- Sets pool size and timeout
- Tests initial connection

```java
public Connection getConnection() throws SQLException
```
- Gets connection from pool
- Automatically manages lifecycle
- Returns connection in milliseconds

```java
public boolean testConnection()
```
- Tests database connectivity
- Executes simple query
- Returns true if connected

```java
public void shutdown()
```
- Closes all connections
- Cleans up resources

#### Configuration

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://localhost:3306/azaala_vcs");
config.setUsername("root");
config.setPassword("password");
config.setMaximumPoolSize(10);
config.setConnectionTimeout(30000);

dataSource = new HikariDataSource(config);
```

#### Example Usage

```java
try {
    Connection conn = connectionPool.getConnection();
    
    try (Statement stmt = conn.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT * FROM repositories");
        // Use ResultSet
    }
    
    // Connection automatically returned to pool
    conn.close();
} catch (SQLException e) {
    e.printStackTrace();
}
```

---

### 25. DatabaseException.java - Custom Exception

**Location**: `com.azaala.vcs.persistence.DatabaseException`
**Extends**: `Exception`

**Purpose**: Custom exception for database-related errors.

#### Constructors

```java
public DatabaseException(String message)
public DatabaseException(String message, Throwable cause)
```

#### Usage

```java
try {
    // Database operation
} catch (SQLException e) {
    throw new DatabaseException("Failed to fetch commits", e);
}
```

---

### 26. DAO & Models Packages

**Location**: `com.azaala.vcs.persistence.dao` and `models`

**Purpose**: Data Access Objects and Entity Models for database persistence.

#### DAO Pattern Structure

```
RepositoryDAO
├── create(Repository)
├── read(id)
├── update(Repository)
├── delete(id)
└── findAll()

CommitDAO
├── create(Commit)
├── findByRepository(repoId)
├── findById(commitId)
└── delete(id)

FileDAO
├── addFile(CommitFile)
├── getFilesForCommit(commitId)
└── deleteFilesForCommit(commitId)
```

#### Entity Models

```java
// RepositoryModel
Long id
String name
String path
LocalDateTime createdAt
String description

// CommitModel
Long id
Long repoId
String commitId
String message
String summary
LocalDateTime createdAt

// FileModel
Long id
Long commitId
String filePath
String fileHash
```

---

## Component Dependencies

### Dependency Graph

```
Main
  └─ Dashboard (GUI)
      ├─ VCS (Core Facade)
      │   ├─ Repository
      │   ├─ FileHandler
      │   ├─ DiffUtil
      │   ├─ SummaryGenerator
      │   └─ CommandHandler
      │       └─ VCS
      ├─ DatabaseManager
      │   ├─ ConnectionPool
      │   └─ DatabaseConfig
      ├─ WorkerFactory
      │   └─ BaseVCSWorker
      │       ├─ ProgressListener
      │       └─ VCS
      └─ [UI Panels]
          ├─ OverviewPanel
          ├─ HistoryPanel
          ├─ StatusPanel
          ├─ DiffPanel
          ├─ SettingsPanel
          └─ UITheme
```

### Dependency Resolution Order

1. **Utility Layer**: Utils, Constants
2. **Model Layer**: Commit, Repository
3. **File Layer**: FileHandler
4. **Database Layer**: DatabaseConfig, ConnectionPool, DatabaseManager
5. **Business Logic**: VCS, DiffUtil, SummaryGenerator, CommandHandler
6. **Async Layer**: ProgressListener, BaseVCSWorker, WorkerFactory
7. **UI Layer**: UITheme, Panels, Dashboard
8. **Entry Point**: Main

---

## Data Flow

### Initialization Flow

```
Main.main()
  ├─ Dashboard()
  │   ├─ DatabaseManager.getInstance().initialize()
  │   │   ├─ ConnectionPool.initialize()
  │   │   └─ DatabaseManager.initializeSchema()
  │   ├─ VCS()
  │   │   ├─ FileHandler()
  │   │   ├─ DiffUtil()
  │   │   └─ SummaryGenerator()
  │   ├─ UITheme.applyTheme()
  │   └─ buildComponents()
  │       ├─ buildMenuBar()
  │       ├─ buildToolBar()
  │       ├─ buildTabbedPane()
  │       └─ buildStatusBar()
  └─ setVisible(true)
```

### Repository Initialization Flow

```
User clicks "Init Repository"
  └─ Dashboard.onInitRepository()
      └─ WorkerFactory.createRepositoryInitWorker()
          └─ RepositoryInitWorker.doInBackground()
              ├─ VCS.initRepository(path)
              │   ├─ Repository(path)
              │   └─ VCS.init()
              │       ├─ FileHandler.createDirectory("data")
              │       ├─ FileHandler.createDirectory("data/commits")
              │       └─ FileHandler.createDirectory("data/index")
              └─ DatabaseManager.saveRepository()
```

### Commit Flow

```
User enters message and clicks "Commit"
  └─ Dashboard.onCommit()
      └─ WorkerFactory.createCommitWorker()
          └─ CommitWorker.doInBackground()
              ├─ VCS.addFile() [for each staged file]
              ├─ VCS.commit(message)
              │   ├─ SummaryGenerator.generateSummary()
              │   ├─ Commit(message, files, summary)
              │   ├─ FileHandler.copyToIndex() [copy files]
              │   ├─ Repository.createCommit()
              │   │   └─ FileHandler.writeCommitMetadata()
              │   └─ DatabaseManager.saveCommit()
              └─ Update UI with results
```

### Diff Flow

```
User selects commits and clicks "Diff"
  └─ Dashboard.onDiff()
      └─ WorkerFactory.createDiffWorker()
          └─ DiffWorker.doInBackground()
              ├─ VCS.generateDiff(commit1, commit2)
              │   └─ DiffUtil.generateDetailedDiff()
              │       ├─ Compare file lists
              │       ├─ Categorize changes
              │       ├─ Format output
              │       └─ Return diff lines
              └─ Display results in DiffPanel
```

---

## Configuration Files

### db.properties

```properties
# Database Configuration
db.host=localhost
db.port=3306
db.name=azaala_vcs
db.user=root
db.password=your_password

# Connection Pool Settings
db.pool.size=10
db.pool.timeout=30000

# Auto-initialization
db.auto.init=true
```

### schema.sql

Contains SQL statements for:
- Creating repositories table
- Creating commits table
- Creating commit_files table
- Creating settings table
- Creating indexes
- Setting up constraints

---

## Error Handling Strategy

### Exception Hierarchy

```
Throwable
  └─ Exception
      ├─ IOException (FileHandler)
      ├─ SQLException (Database)
      ├─ SecurityException (Utils)
      ├─ IllegalArgumentException (Validation)
      └─ DatabaseException (Custom)
```

### Error Handling Patterns

**Try-Catch-Log**
```java
try {
    // Operation
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
    e.printStackTrace();
    return null/false;
}
```

**Try-Catch-Notify**
```java
try {
    // Operation
} catch (Exception e) {
    progressListener.onError("Operation failed", e);
}
```

**Try-Catch-Rethrow**
```java
try {
    // Operation
} catch (SQLException e) {
    throw new DatabaseException("DB Error", e);
}
```

---

This comprehensive documentation covers all 26+ major components of the Azaala VCS system with detailed descriptions, code examples, and usage patterns.

