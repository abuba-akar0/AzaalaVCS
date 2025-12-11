# 🏛️ Azaala VCS - Architecture & Design Patterns

Comprehensive architecture and design patterns documentation for Azaala VCS.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Design Patterns](#design-patterns)
3. [Layered Architecture](#layered-architecture)
4. [Module Organization](#module-organization)
5. [Data Models](#data-models)
6. [Communication Patterns](#communication-patterns)
7. [Error Handling Architecture](#error-handling-architecture)
8. [Threading Model](#threading-model)
9. [Database Architecture](#database-architecture)
10. [Extensibility Points](#extensibility-points)

---

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Presentation Layer                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Swing GUI (Dashboard)                                   │  │
│  │  ├─ Menu Bar                                             │  │
│  │  ├─ Toolbar                                              │  │
│  │  ├─ Tabbed Panels (Overview, History, Status, Diff)      │  │
│  │  └─ Status Bar                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Application Layer                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  VCS (Facade)                                            │  │
│  │  ├─ Repository Management                               │  │
│  │  ├─ File Operations                                      │  │
│  │  ├─ Commit Management                                    │  │
│  │  └─ Diff & Summary Generation                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  CommandHandler                                          │  │
│  │  ├─ Command Parsing                                      │  │
│  │  ├─ Command Validation                                   │  │
│  │  └─ Command Execution                                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Async/Threading Layer                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  SwingWorker Framework                                   │  │
│  │  ├─ BaseVCSWorker (Abstract)                             │  │
│  │  ├─ Worker Implementations                               │  │
│  │  ├─ WorkerFactory                                        │  │
│  │  └─ ProgressListener Interface                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Business Logic Layer                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Core VCS Classes                                        │  │
│  │  ├─ Repository                                           │  │
│  │  ├─ Commit                                               │  │
│  │  ├─ FileHandler                                          │  │
│  │  ├─ DiffUtil                                             │  │
│  │  ├─ SummaryGenerator                                     │  │
│  │  └─ Utils                                                │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   Persistence Layer                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Database Access                                         │  │
│  │  ├─ DatabaseManager (Singleton)                          │  │
│  │  ├─ DatabaseConfig (Singleton)                           │  │
│  │  ├─ ConnectionPool (HikariCP)                            │  │
│  │  ├─ DAO Classes                                          │  │
│  │  └─ Entity Models                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  File System Operations                                  │  │
│  │  ├─ Repository Directory Structure                       │  │
│  │  ├─ Commit Snapshots                                     │  │
│  │  └─ Index/Staging Area                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                  External Resources                             │
│  ├─ MySQL Database                                              │
│  ├─ File System                                                 │
│  └─ Operating System                                            │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Principles

1. **Separation of Concerns**: Clear separation between UI, business logic, and data access
2. **Single Responsibility**: Each class has one reason to change
3. **Dependency Inversion**: Depend on abstractions (interfaces), not concrete implementations
4. **Open/Closed Principle**: Open for extension, closed for modification
5. **Interface Segregation**: Clients depend only on interfaces they use

---

## Design Patterns

### 1. Facade Pattern

**Location**: `VCS.java`
**Purpose**: Provide unified interface to complex subsystems

```java
public class VCS {
    private Repository repository;
    private FileHandler fileHandler;
    private DiffUtil diffUtil;
    private SummaryGenerator summaryGenerator;
    
    // Simple interface to complex operations
    public boolean initRepository(String path) { ... }
    public boolean addFile(String filePath) { ... }
    public boolean commit(String message) { ... }
    public List<Commit> getCommitHistory() { ... }
}
```

**Benefits**:
- Clients don't know implementation details
- Easy to modify internal implementation
- Reduces coupling between components

---

### 2. Singleton Pattern

**Locations**: `DatabaseManager`, `DatabaseConfig`, `ConnectionPool`, `UITheme`
**Purpose**: Ensure only one instance of a class exists globally

```java
// DatabaseManager
private static DatabaseManager instance;

public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
        instance = new DatabaseManager();
    }
    return instance;
}
```

**When Used**:
- Database connections (ensure single pool)
- Configuration (single source of truth)
- UI theme (consistent appearance)

**Thread Safety**:
```java
// Using synchronized block
public static synchronized DatabaseManager getInstance() { ... }

// Alternative: Eager initialization
private static final DatabaseManager instance = new DatabaseManager();
public static DatabaseManager getInstance() { return instance; }
```

---

### 3. Factory Pattern

**Location**: `WorkerFactory.java`
**Purpose**: Create objects without specifying exact classes

```java
public class WorkerFactory {
    
    public static BaseVCSWorker<?> createRepositoryInitWorker(
        String path, ProgressListener listener) {
        return new RepositoryInitWorker(path, listener);
    }
    
    public static BaseVCSWorker<?> createCommitWorker(
        String message, ProgressListener listener) {
        return new CommitWorker(message, listener);
    }
}
```

**Benefits**:
- Decouples client from concrete worker classes
- Centralized worker creation logic
- Easy to swap implementations
- Consistent worker initialization

**Usage**:
```java
BaseVCSWorker<?> worker = WorkerFactory.createCommitWorker(message, listener);
worker.execute();
```

---

### 4. Observer Pattern

**Location**: `ProgressListener` interface
**Purpose**: Notify observers of state changes in async operations

```java
public interface ProgressListener {
    void onProgress(String message, int progress);
    void onSuccess(String result);
    void onError(String message, Throwable error);
}

// Implementation
class UIProgressListener implements ProgressListener {
    @Override
    public void onProgress(String message, int progress) {
        progressBar.setValue(progress);
    }
    
    @Override
    public void onSuccess(String result) {
        statusLabel.setText("✓ " + result);
    }
    
    @Override
    public void onError(String message, Throwable error) {
        errorLabel.setText("✗ " + message);
    }
}
```

**Benefits**:
- Loose coupling between worker and observer
- Multiple observers supported
- Dynamic observer registration

---

### 5. Command Pattern

**Location**: `CommandHandler.java`
**Purpose**: Encapsulate requests as objects

```java
public class CommandHandler {
    public boolean executeCommand(String command, String[] args) {
        switch (command.toLowerCase()) {
            case "init":
                return handleInit(args);
            case "add":
                return handleAdd(args);
            case "commit":
                return handleCommit(args);
            // ... more commands
        }
    }
    
    private boolean handleInit(String[] args) { ... }
    private boolean handleAdd(String[] args) { ... }
    private boolean handleCommit(String[] args) { ... }
}
```

**Benefits**:
- Easy to add new commands
- Commands can be queued/logged
- Undo/redo possible

---

### 6. Data Access Object (DAO) Pattern

**Location**: `com.azaala.vcs.persistence.dao`
**Purpose**: Abstract data access from business logic

```java
public interface RepositoryDAO {
    Long create(RepositoryModel repo) throws DatabaseException;
    RepositoryModel read(Long id) throws DatabaseException;
    void update(RepositoryModel repo) throws DatabaseException;
    void delete(Long id) throws DatabaseException;
    List<RepositoryModel> findAll() throws DatabaseException;
}

public class RepositoryDAOImpl implements RepositoryDAO {
    // Implementation using ConnectionPool
}
```

**Benefits**:
- Database logic isolated
- Easy to switch databases
- Unit testing with mock DAOs
- Consistent data access patterns

---

### 7. Template Method Pattern

**Location**: `BaseVCSWorker.java`
**Purpose**: Define skeleton of algorithm, let subclasses fill details

```java
public abstract class BaseVCSWorker<T> extends SwingWorker<T, String> {
    
    @Override
    protected final void done() {
        try {
            T result = get();
            onSuccess(result);
        } catch (ExecutionException e) {
            onError((Exception) e.getCause());
        }
    }
    
    protected abstract void onSuccess(T result);
    protected abstract void onError(Exception e);
}
```

**Benefits**:
- Consistent error handling
- Reduces code duplication
- Enforces contract for subclasses

---

### 8. Strategy Pattern

**Location**: `DiffUtil`, `SummaryGenerator`
**Purpose**: Define family of algorithms, make them interchangeable

```java
// Strategy interface (implicit)
public class DiffUtil {
    public List<String> generateDetailedDiff(...) { ... }
    public String compareFiles(...) { ... }
}

public class SummaryGenerator {
    public String generateSummary(Commit current, Commit previous) { ... }
}

// Client can choose strategy
DiffUtil detailedDiff = new DiffUtil();
List<String> diffs = detailedDiff.generateDetailedDiff(...);
```

---

### 9. Builder Pattern

**Location**: `Dialog and Component Construction`
**Purpose**: Construct complex objects step by step

```java
// Example: Building preferences dialog
public class PreferencesDialog extends JDialog {
    private PreferencesDialog() {
        // Private constructor
    }
    
    public static PreferencesDialog create(Component parent) {
        PreferencesDialog dialog = new PreferencesDialog();
        dialog.buildComponents();
        dialog.setupLayout();
        dialog.applyTheme();
        dialog.loadPreferences();
        return dialog;
    }
}
```

---

## Layered Architecture

### Layer 1: Presentation Layer

**Components**: `Dashboard`, `Panels`, `UITheme`
**Responsibilities**:
- User interface rendering
- User input collection
- Event handling
- Display results

**Key Characteristics**:
- No business logic
- No direct database access
- Communicates with application layer via VCS/CommandHandler

```java
public class Dashboard extends JFrame {
    private VCS vcs;  // Application layer
    
    private void onCommit() {
        String message = getInputFromUser();
        WorkerFactory.createCommitWorker(message, progressListener)
            .execute();
    }
}
```

---

### Layer 2: Application/Control Layer

**Components**: `VCS`, `CommandHandler`
**Responsibilities**:
- Coordinate operations
- Manage workflow
- Validate inputs
- Route requests

```java
public class VCS {
    public boolean commit(String message) {
        // Validate
        if (!repository.isInitialized()) return false;
        
        // Coordinate
        List<String> files = getStagedFiles();
        Commit commit = new Commit(message, files);
        
        // Persist
        repository.createCommit(commit);
        
        // Cleanup
        clearStagingArea();
        
        return true;
    }
}
```

---

### Layer 3: Business Logic Layer

**Components**: `Repository`, `Commit`, `FileHandler`, `DiffUtil`, `SummaryGenerator`
**Responsibilities**:
- Core domain logic
- Business rule enforcement
- Data transformation
- Complex calculations

```java
public class DiffUtil {
    public List<String> generateDetailedDiff(Commit c1, Commit c2, String path) {
        // Categorize files
        Set<String> added = new HashSet<>(c2.getChangedFiles());
        added.removeAll(c1.getChangedFiles());
        
        // Generate output
        List<String> output = new ArrayList<>();
        output.add("╔═══════════════════════════════════╗");
        output.add("║ DETAILED COMMIT COMPARISON        ║");
        // ... more logic
        
        return output;
    }
}
```

---

### Layer 4: Async/Threading Layer

**Components**: `BaseVCSWorker`, `Worker Implementations`, `WorkerFactory`
**Responsibilities**:
- Background task execution
- Progress reporting
- Error handling in background
- UI thread safety

```java
public class CommitWorker extends BaseVCSWorker<String> {
    @Override
    protected String doInBackground() throws Exception {
        publishProgress("Creating commit...", 25);
        
        // Long operation
        String commitId = vcs.commit(message);
        
        publishProgress("Persisting...", 75);
        
        // Update database
        DatabaseManager.getInstance().saveCommit(commitId);
        
        return "Commit created: " + commitId;
    }
}
```

---

### Layer 5: Persistence Layer

**Components**: `DatabaseManager`, `ConnectionPool`, `DAO`, `Models`
**Responsibilities**:
- Data storage/retrieval
- Database connection management
- Query execution
- Transaction management

```java
public class RepositoryDAO {
    public Long create(RepositoryModel repo) throws DatabaseException {
        String sql = "INSERT INTO repositories (name, path, created_at) VALUES (?, ?, ?)";
        
        try (Connection conn = connectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, repo.getName());
            stmt.setString(2, repo.getPath());
            stmt.setTimestamp(3, Timestamp.valueOf(repo.getCreatedAt()));
            
            stmt.executeUpdate();
            // Return generated ID
        }
    }
}
```

---

## Module Organization

### Module Structure

```
com.azaala.vcs/
├── core/
│   ├── VCS.java
│   ├── Repository.java
│   ├── Commit.java
│   └── CommandHandler.java
├── operations/
│   ├── FileHandler.java
│   ├── DiffUtil.java
│   └── SummaryGenerator.java
├── utilities/
│   └── Utils.java
├── async/
│   ├── BaseVCSWorker.java
│   ├── RepositoryInitWorker.java
│   ├── CommitWorker.java
│   ├── WorkerFactory.java
│   └── ProgressListener.java
├── gui/
│   ├── Dashboard.java
│   ├── OverviewPanel.java
│   ├── HistoryPanel.java
│   ├── StatusPanel.java
│   ├── DiffPanel.java
│   ├── SettingsPanel.java
│   ├── PreferencesDialog.java
│   └── UITheme.java
├── persistence/
│   ├── DatabaseManager.java
│   ├── DatabaseConfig.java
│   ├── ConnectionPool.java
│   ├── DatabaseException.java
│   ├── dao/
│   │   ├── RepositoryDAO.java
│   │   ├── CommitDAO.java
│   │   └── FileDAO.java
│   └── models/
│       ├── RepositoryModel.java
│       ├── CommitModel.java
│       └── FileModel.java
└── Main.java
```

### Dependency Management

**Internal Dependencies** (Package-level):
- `core` depends on: `utilities`
- `operations` depends on: `core`, `utilities`
- `async` depends on: `core`, `operations`
- `gui` depends on: `async`, `core`, `persistence`
- `persistence` depends on: `core`, `utilities`

**No Circular Dependencies**:
- Ensures clean architecture
- Easier testing
- Better maintainability

---

## Data Models

### Repository Model

```java
public class Repository {
    private String repositoryPath;      // Full path
    private String name;                // Name from path
    private LocalDateTime createdAt;    // Timestamp
    private List<Commit> commits;       // All commits
    private Long repoId;                // Database ID
}
```

**Responsibilities**:
- Store repository metadata
- Manage commit collection
- Track repository state

---

### Commit Model

```java
public class Commit {
    private String commitId;            // Unique ID (8 chars)
    private String message;             // User message
    private String summary;             // AI-generated summary
    private LocalDateTime timestamp;    // Creation time
    private List<String> changedFiles;  // Files in commit
}
```

**Constraints**:
- commitId: not null, unique
- message: 1-500 characters
- changedFiles: not null, not empty

---

### Database Models

```java
public class RepositoryModel {
    private Long id;                    // Primary key
    private String name;
    private String path;
    private LocalDateTime createdAt;
    private String description;
}

public class CommitModel {
    private Long id;                    // Primary key
    private Long repoId;                // Foreign key
    private String commitId;
    private String message;
    private String summary;
    private LocalDateTime createdAt;
}

public class FileModel {
    private Long id;                    // Primary key
    private Long commitId;              // Foreign key
    private String filePath;
    private String fileHash;
}
```

---

## Communication Patterns

### GUI to VCS Communication

```
User Action (Click Button)
    ↓
Event Handler
    ↓
WorkerFactory.createWorker()
    ↓
BaseVCSWorker.execute()
    ↓ [Background Thread]
doInBackground() {
    VCS.operation()
    publishProgress()
}
    ↓ [EDT]
done()
    ↓
ProgressListener callbacks
    ↓
Update UI
```

### Example: Commit Flow

```java
// UI Layer
dashboard.onCommitButton() {
    String message = commitDialog.getMessage();
    
    // Create async worker
    CommitWorker worker = WorkerFactory.createCommitWorker(
        message,
        progressListener
    );
    worker.execute();
}

// Async Layer
CommitWorker.doInBackground() {
    publishProgress("Creating commit...", 25);
    
    // Call business logic
    boolean success = vcs.commit(message);
    
    publishProgress("Persisting...", 75);
    
    // Save to database
    DatabaseManager.getInstance().saveCommit(...);
    
    return "Success";
}

// UI Layer (EDT)
progressListener.onSuccess("Commit created");
dashboard.refresh();
```

---

## Error Handling Architecture

### Exception Hierarchy

```
Exception
├── IOException (File operations)
├── SQLException (Database operations)
├── SecurityException (Security checks)
├── IllegalArgumentException (Validation)
└── DatabaseException (Custom)
```

### Handling Strategies

**1. Validation (Preventive)**
```java
public Commit(String message, List<String> files) {
    if (message == null || message.isEmpty()) {
        throw new IllegalArgumentException("Message cannot be empty");
    }
    if (files == null || files.isEmpty()) {
        throw new IllegalArgumentException("No files in commit");
    }
}
```

**2. Try-Catch (Reactive)**
```java
try {
    String content = fileHandler.readFile(path);
} catch (IOException e) {
    System.err.println("Failed to read file: " + e.getMessage());
    return null;
}
```

**3. Rethrow with Context**
```java
try {
    // Database operation
} catch (SQLException e) {
    throw new DatabaseException("Failed to save commit", e);
}
```

**4. UI Notification (User Feedback)**
```java
@Override
public void onError(String message, Throwable error) {
    JOptionPane.showErrorDialog(null, message);
    logger.error(message, error);
}
```

---

## Threading Model

### Thread Distribution

```
Main Thread (EDT)
├── Window creation
├── Event handling
├── UI updates
└── Listener notifications

SwingWorker Threads (Thread Pool)
├── File I/O operations
├── Database operations
├── Diff generation
├── Summary generation
└── Long-running tasks

Database Connection Pool
└── Multiple connections for concurrent access
```

### Thread Safety Mechanisms

**1. SwingWorker (Built-in)**
```java
public class CommitWorker extends SwingWorker<String, String> {
    @Override
    protected String doInBackground() {
        // Runs on background thread
        return vcs.commit(message);
    }
    
    @Override
    protected void done() {
        // Automatically runs on EDT
        updateUI();
    }
}
```

**2. Synchronized Singleton**
```java
public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
        instance = new DatabaseManager();
    }
    return instance;
}
```

**3. HikariCP Connection Pool**
```java
// Thread-safe connection management
Connection conn = connectionPool.getConnection();
// Use connection
conn.close(); // Returns to pool
```

---

## Database Architecture

### Schema Design

```sql
repositories
├── Primary Key: id
├── Unique: path
└── Foreign Keys: none

commits
├── Primary Key: id
├── Foreign Key: repo_id → repositories(id)
├── Unique: commit_id
└── Index: (repo_id, created_at)

commit_files
├── Primary Key: id
├── Foreign Key: commit_id → commits(id)
└── Index: commit_id

settings
├── Primary Key: id
├── Foreign Key: repo_id → repositories(id)
└── Index: (repo_id, key_name)
```

### Connection Pooling

```
HikariCP Pool (10 connections)
├── Available (0-10)
│   └── Waiting threads get connections
├── In Use (0-10)
│   └── Executing queries
└── Closed/Invalid
    └── Replaced automatically
```

### Transaction Management

```java
try (Connection conn = connectionPool.getConnection()) {
    conn.setAutoCommit(false);
    
    // Operations
    stmt1.execute();
    stmt2.execute();
    
    conn.commit();
} catch (Exception e) {
    conn.rollback();
    throw new DatabaseException("Transaction failed", e);
}
```

---

## Extensibility Points

### 1. Adding New Commands

**Step 1**: Add to CommandHandler
```java
// In VALID_COMMANDS list
private static final List<String> VALID_COMMANDS = Arrays.asList(
    "init", "add", "commit", "status", "log", "diff", 
    "help", "version", "exit",
    "branch"  // New command
);

// In executeCommand()
case "branch":
    return handleBranch(args);

// Add handler method
private boolean handleBranch(String[] args) {
    // Implementation
}
```

**Step 2**: Implement in VCS
```java
public List<String> listBranches() {
    // Implementation
}
```

---

### 2. Adding New Worker Types

**Step 1**: Extend BaseVCSWorker
```java
public class BranchWorker extends BaseVCSWorker<String> {
    @Override
    protected String doInBackground() throws Exception {
        // Background work
        return "Result";
    }
    
    @Override
    protected void onSuccess(String result) {
        // Handle success
    }
}
```

**Step 2**: Add to WorkerFactory
```java
public static BaseVCSWorker<?> createBranchWorker(
    String branchName, ProgressListener listener) {
    return new BranchWorker(branchName, listener);
}
```

---

### 3. Adding New GUI Panels

**Step 1**: Create Panel Class
```java
public class BranchPanel extends JPanel {
    public BranchPanel() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() { }
    private void setupLayout() { }
}
```

**Step 2**: Add to Dashboard
```java
private BranchPanel branchPanel;

public Dashboard() {
    // ...
    branchPanel = new BranchPanel();
    // ...
}

private void buildTabbedPane() {
    tabbedPane.addTab("Branches", branchPanel);
}
```

---

### 4. Changing Database

**Requirements**:
1. Update `DatabaseConfig` properties
2. Create/update schema for target database
3. Update JDBC URL/Driver in `ConnectionPool`

**Example - Switch to PostgreSQL**:
```java
// In ConnectionPool
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost:5432/azaala_vcs");
config.setDriverClassName("org.postgresql.Driver");
// ... rest same as MySQL
```

---

### 5. Adding Custom Diff Strategy

**Extend DiffUtil**:
```java
public List<String> generateCustomDiff(...) {
    // Custom implementation
}

// Or create separate class
public class AdvancedDiffUtil extends DiffUtil {
    @Override
    public List<String> generateDetailedDiff(...) {
        // Enhanced implementation
    }
}
```

---

This architecture documentation provides a comprehensive overview of how Azaala VCS is structured, the patterns it uses, and how it can be extended.

