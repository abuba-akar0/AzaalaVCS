# 🚀 Azaala VCS - Developer Quick Start Guide

Quick reference guide for developers working with Azaala VCS codebase.

---

## Table of Contents

1. [Environment Setup](#environment-setup)
2. [Building the Project](#building-the-project)
3. [Running the Application](#running-the-application)
4. [Project Structure](#project-structure)
5. [Development Workflow](#development-workflow)
6. [Common Tasks](#common-tasks)
7. [Testing Guidelines](#testing-guidelines)
8. [Debugging Tips](#debugging-tips)
9. [Code Style](#code-style)
10. [Performance Tips](#performance-tips)

---

## Environment Setup

### Prerequisites

```bash
# Check Java version (11+)
java -version
# Output: openjdk version "11.0.x" or newer

# Check Maven version (3.6+)
mvn -version
# Output: Apache Maven 3.6.x

# Check MySQL version (8.0+)
mysql --version
# Output: mysql Ver 8.0.x
```

### JDK Installation

**Windows**:
1. Download JDK 11+ from oracle.com
2. Install to `C:\Program Files\Java\jdk-11.x`
3. Add to PATH: `JAVA_HOME=C:\Program Files\Java\jdk-11.x`

**Linux**:
```bash
sudo apt-get install openjdk-11-jdk
```

**macOS**:
```bash
brew install openjdk@11
```

### Maven Installation

**Windows**:
1. Download Maven from maven.apache.org
2. Extract to `C:\Program Files\apache-maven-3.8.x`
3. Add to PATH: `MAVEN_HOME=C:\Program Files\apache-maven-3.8.x`

**Linux/macOS**:
```bash
brew install maven  # macOS
sudo apt-get install maven  # Linux
```

### MySQL Setup

**Windows**:
1. Download MySQL Community Server from mysql.com
2. Run installer
3. Configure as service

**Linux**:
```bash
sudo apt-get install mysql-server
sudo mysql_secure_installation
```

**macOS**:
```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

### Configure Database

```sql
-- Create database
CREATE DATABASE azaala_vcs;

-- Create user
CREATE USER 'azaala'@'localhost' IDENTIFIED BY 'password';

-- Grant privileges
GRANT ALL PRIVILEGES ON azaala_vcs.* TO 'azaala'@'localhost';
FLUSH PRIVILEGES;
```

### IDE Setup

**IntelliJ IDEA** (Recommended):
1. Install IntelliJ IDEA Community Edition
2. Open project folder
3. Maven will auto-detect pom.xml
4. Project structure: File → Project Structure → Modules

**Eclipse**:
1. Install Eclipse IDE for Java Developers
2. File → Open Projects from File System
3. Select project root

**VS Code**:
1. Install Extension Pack for Java
2. Install Maven for Java
3. Open project folder

---

## Building the Project

### Clean Build

```bash
# Navigate to project root
cd /path/to/Azaala_VCS

# Clean previous builds
mvn clean

# Compile source code
mvn compile

# Run tests
mvn test

# Package application
mvn package
```

### Build with Skip Tests

```bash
mvn clean package -DskipTests
```

### Build Single Module

```bash
# Build specific package only
mvn clean compile -pl :core
```

### Check Build Issues

```bash
# Verbose output
mvn clean compile -X

# Display dependencies
mvn dependency:tree
```

### Common Build Errors

**Error**: `[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.1:compile`
**Solution**: Check Java version: `java -version` should be 11+

**Error**: `[ERROR] Missing artifact mysql:mysql-connector-java:jar:8.0.33`
**Solution**: Run `mvn dependency:resolve` to download dependencies

---

## Running the Application

### GUI Mode

```bash
# Build first
mvn clean package -DskipTests

# Run JAR
java -jar target/azaala-vcs-2.0.0.jar

# Or from IDE
# - Right-click Main.java → Run 'Main.main()'
```

### Console Mode

```bash
java -jar target/azaala-vcs-2.0.0.jar -c
```

### With Logging

```bash
# Enable debug logging
java -Dlog.level=DEBUG -jar target/azaala-vcs-2.0.0.jar
```

### With Custom JVM Args

```bash
# Increase memory
java -Xmx2G -Xms512M -jar target/azaala-vcs-2.0.0.jar

# Enable assertions
java -ea -jar target/azaala-vcs-2.0.0.jar
```

---

## Project Structure

### Directory Layout

```
Azaala_VCS/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/azaala/vcs/
│   │   │       ├── Main.java (30 lines)
│   │   │       ├── VCS.java (300+ lines)
│   │   │       ├── Repository.java (200+ lines)
│   │   │       ├── Commit.java (150+ lines)
│   │   │       ├── FileHandler.java (200+ lines)
│   │   │       ├── DiffUtil.java (300+ lines)
│   │   │       ├── SummaryGenerator.java (150+ lines)
│   │   │       ├── CommandHandler.java (250+ lines)
│   │   │       ├── Utils.java (150+ lines)
│   │   │       ├── async/
│   │   │       │   ├── BaseVCSWorker.java (100+ lines)
│   │   │       │   ├── RepositoryInitWorker.java
│   │   │       │   ├── CommitWorker.java
│   │   │       │   └── ... (8 worker classes)
│   │   │       ├── gui/
│   │   │       │   ├── Dashboard.java (400+ lines)
│   │   │       │   ├── OverviewPanel.java
│   │   │       │   ├── HistoryPanel.java
│   │   │       │   ├── StatusPanel.java
│   │   │       │   ├── DiffPanel.java
│   │   │       │   ├── SettingsPanel.java
│   │   │       │   ├── PreferencesDialog.java
│   │   │       │   └── UITheme.java
│   │   │       └── persistence/
│   │   │           ├── DatabaseManager.java
│   │   │           ├── DatabaseConfig.java
│   │   │           ├── ConnectionPool.java
│   │   │           ├── DatabaseException.java
│   │   │           ├── dao/
│   │   │           └── models/
│   │   └── resources/
│   │       ├── db.properties
│   │       └── schema.sql
│   └── test/
│       └── java/
├── target/
│   ├── classes/ (Compiled .class files)
│   └── azaala-vcs-2.0.0.jar
├── docs/
│   ├── COMPONENT_DOCUMENTATION.md
│   ├── ARCHITECTURE_DESIGN.md
│   ├── API_REFERENCE.md
│   └── ... (other docs)
├── pom.xml
├── Readme.md
└── .gitignore
```

### File Size Guidelines

```
Small files (< 100 lines):
- Utils.java
- Main.java
- ProgressListener.java

Medium files (100-300 lines):
- Commit.java
- CommandHandler.java
- SummaryGenerator.java
- FileHandler.java
- BaseVCSWorker.java

Large files (300+ lines):
- VCS.java
- DiffUtil.java
- Repository.java
- Dashboard.java
```

---

## Development Workflow

### Creating a New Feature

**Step 1: Create Feature Branch**
```bash
git checkout -b feature/new-feature
```

**Step 2: Identify Affected Classes**
- For example, adding a "blame" feature affects:
  - `VCS.java` - Add new method
  - `CommandHandler.java` - Add new command
  - `DiffUtil.java` - Add new utility (maybe)
  - New class: `BlameWorker.java`

**Step 3: Write Tests First**
```java
// In test/
public class BlameTest {
    @Test
    public void testBlameGeneration() {
        // Test new functionality
    }
}
```

**Step 4: Implement Feature**
- Follow existing patterns
- Use same error handling strategy
- Add Javadoc comments

**Step 5: Update Documentation**
- Update COMPONENT_DOCUMENTATION.md
- Update API_REFERENCE.md
- Add code examples

**Step 6: Test Integration**
```bash
mvn clean package
java -jar target/azaala-vcs-2.0.0.jar -c blame <args>
```

**Step 7: Commit and Push**
```bash
git add .
git commit -m "feat: Add blame functionality"
git push origin feature/new-feature
```

### Modifying Existing Code

**Before Modifying**:
1. Check if method is used elsewhere: `Ctrl+Shift+F` (Find All)
2. Review tests for method
3. Check documentation

**After Modifying**:
1. Update related tests
2. Run: `mvn clean compile test`
3. Update documentation
4. Verify backward compatibility

---

## Common Tasks

### Adding a New Command

**1. Add to CommandHandler.java**:
```java
private static final List<String> VALID_COMMANDS = Arrays.asList(
    "init", "add", "commit", "status", "log", "diff",
    "help", "version", "exit",
    "blame"  // NEW
);

case "blame":
    return handleBlame(args);

private boolean handleBlame(String[] args) {
    if (args.length < 1) {
        System.err.println("Usage: blame <filepath>");
        return false;
    }
    
    String filepath = args[0];
    List<String> blameInfo = vcs.generateBlame(filepath);
    for (String line : blameInfo) {
        System.out.println(line);
    }
    return true;
}
```

**2. Add to VCS.java**:
```java
public List<String> generateBlame(String filepath) {
    // Implementation
}
```

**3. Update COMPONENT_DOCUMENTATION.md**:
```markdown
#### Blame Generation
- Command: `blame <filepath>`
- Returns line-by-line commit info
```

### Adding a New Worker

**1. Create Worker Class**:
```java
// In async/BlameWorker.java
public class BlameWorker extends BaseVCSWorker<List<String>> {
    private String filepath;
    
    public BlameWorker(String filepath, ProgressListener listener) {
        super(listener);
        this.filepath = filepath;
    }
    
    @Override
    protected List<String> doInBackground() throws Exception {
        publishProgress("Generating blame...", 0);
        return vcs.generateBlame(filepath);
    }
    
    @Override
    protected void onSuccess(List<String> result) {
        System.out.println("✓ Blame info generated: " + result.size() + " lines");
    }
}
```

**2. Add to WorkerFactory.java**:
```java
public static BaseVCSWorker<?> createBlameWorker(
    String filepath, ProgressListener listener) {
    return new BlameWorker(filepath, listener);
}
```

**3. Use in GUI**:
```java
// In Dashboard.java
BaseVCSWorker<?> worker = WorkerFactory.createBlameWorker(filepath, listener);
worker.execute();
```

### Adding a New Panel

**1. Create Panel Class**:
```java
// In gui/BlamePanel.java
public class BlamePanel extends JPanel {
    private JTextArea blameArea;
    private JComboBox<String> fileSelector;
    private VCS vcs;
    
    public BlamePanel(VCS vcs) {
        this.vcs = vcs;
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        fileSelector = new JComboBox<>();
        blameArea = new JTextArea();
        blameArea.setEditable(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(new JScrollPane(blameArea), BorderLayout.CENTER);
    }
    
    public void displayBlameInfo(String filepath) {
        List<String> blameInfo = vcs.generateBlame(filepath);
        for (String line : blameInfo) {
            blameArea.append(line + "\n");
        }
    }
}
```

**2. Add to Dashboard.java**:
```java
private BlamePanel blamePanel;

public Dashboard() {
    // ...
    blamePanel = new BlamePanel(vcs);
    // ...
}

private void buildTabbedPane() {
    tabbedPane.addTab("Blame", blamePanel);
}
```

---

## Testing Guidelines

### Unit Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=RepositoryTest

# Run test method
mvn test -Dtest=RepositoryTest#testInitRepository
```

### Test Structure

```java
public class VCSTest {
    
    private VCS vcs;
    private Repository repo;
    
    @Before
    public void setUp() {
        // Initialize test fixtures
        vcs = new VCS();
        repo = new Repository("target/test-repo");
    }
    
    @Test
    public void testInitRepository() {
        assertTrue(vcs.initRepository("target/test-repo"));
        assertTrue(repo.isInitialized());
    }
    
    @Test
    public void testAddFile() {
        vcs.initRepository("target/test-repo");
        assertTrue(vcs.addFile("pom.xml"));
    }
    
    @After
    public void tearDown() {
        // Cleanup
        FileUtils.deleteDirectory(new File("target/test-repo"));
    }
}
```

### Integration Testing

```bash
# Full integration test
mvn verify

# With database tests
mvn verify -Pdb-tests
```

---

## Debugging Tips

### Enable Debug Logging

**Via System Property**:
```bash
java -Dlog.level=DEBUG -jar target/azaala-vcs-2.0.0.jar
```

**Via Code**:
```java
// In Main.java or wherever needed
System.setProperty("log.level", "DEBUG");
```

### IntelliJ Debugging

**1. Set Breakpoint**:
- Click on line number (left margin)
- Red dot appears

**2. Debug Run**:
- Right-click Main.java → Debug 'Main.main()'
- Or: Shift+F9 (after clicking in editor)

**3. Step Through Code**:
- F10: Step over
- F11: Step into
- Shift+F11: Step out
- F9: Resume

**4. Watch Expressions**:
- Debugger → Variables tab
- Right-click variable → Watch

### Common Debug Scenarios

**Debugging File Operations**:
```java
// Add print statements
System.out.println("Reading from: " + filePath);
System.out.println("File exists: " + new File(filePath).exists());
String content = fileHandler.readFile(filePath);
System.out.println("Content length: " + (content != null ? content.length() : "null"));
```

**Debugging Database Issues**:
```java
try {
    DatabaseManager.getInstance().initialize();
    System.out.println("✓ DB initialized");
} catch (DatabaseException e) {
    System.err.println("✗ DB error: " + e.getMessage());
    e.printStackTrace();
}
```

**Debugging Async Issues**:
```java
// Add logging to worker
protected String doInBackground() throws Exception {
    System.out.println("[Thread] " + Thread.currentThread().getName());
    publishProgress("Step 1", 25);
    System.out.println("Progress published");
    return "Done";
}
```

---

## Code Style

### Naming Conventions

```java
// Classes: PascalCase
public class VCS { }
public class FileHandler { }

// Methods: camelCase
public boolean initRepository(String path) { }
public List<Commit> getCommitHistory() { }

// Variables: camelCase
private String repositoryPath;
private List<Commit> commits;

// Constants: UPPER_SNAKE_CASE
private static final String DATA_DIR = "data";
private static final int MAX_MESSAGE_LENGTH = 500;
```

### Javadoc Format

```java
/**
 * Initializes a new repository at the specified path.
 * Creates necessary directory structure and configuration files.
 * 
 * @param path Directory path for the repository
 * @return true if initialization was successful, false otherwise
 * @throws IllegalArgumentException if path is null or empty
 * @throws SecurityException if insufficient permissions
 * 
 * @see Repository#isInitialized()
 */
public boolean initRepository(String path) {
    // Implementation
}
```

### Method Length

```java
// Good: Methods under 30 lines
public boolean commit(String message) {
    if (!validate(message)) return false;
    
    List<String> files = getStagedFiles();
    if (files.isEmpty()) return false;
    
    Commit commit = new Commit(message, files);
    repository.createCommit(commit);
    clearStagingArea();
    
    return true;
}

// Better: Extract long methods
private List<String> createCommitIfValid(String message) {
    List<String> files = getStagedFiles();
    if (!validateForCommit(message, files)) {
        return Collections.emptyList();
    }
    
    Commit commit = new Commit(message, files);
    repository.createCommit(commit);
    return files;
}
```

### Error Handling

```java
// Good: Specific exceptions
try {
    return Files.readString(path);
} catch (IOException e) {
    System.err.println("Error reading file: " + e.getMessage());
    return null;
}

// Better: With logging
try {
    return Files.readString(path);
} catch (IOException e) {
    logger.error("Failed to read file: " + path, e);
    throw new RepositoryException("Cannot read file", e);
}

// Bad: Catching generic Exception
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();  // Don't do this!
}
```

---

## Performance Tips

### Database Optimization

```java
// Batch operations
for (String file : files) {
    vcs.addFile(file);  // Slow: Multiple DB calls
}

// Better: Use batch method
vcs.addAllFiles(directory);  // Optimized
```

### File I/O Optimization

```java
// Avoid repeated reads
String content = fileHandler.readFile(path);
String content2 = fileHandler.readFile(path);  // Redundant

// Better: Reuse result
String content = fileHandler.readFile(path);
if (content != null) {
    processContent(content);
    generateDiff(content);
}
```

### Connection Pool Tuning

```properties
# In db.properties
db.pool.size=20  # Increase for high concurrency
db.pool.timeout=60000  # Increase for long operations
```

### Memory Management

```java
// Clear large collections when done
List<String> largeList = getAllCommits();
// Process...
largeList.clear();  // Or: largeList = null;

// Avoid string concatenation in loops
StringBuilder sb = new StringBuilder();
for (Commit c : commits) {
    sb.append(c.getMessage()).append("\n");  // Good
    // result += c.getMessage();  // BAD: Creates new string each iteration
}
String result = sb.toString();
```

---

## Quick Reference Commands

```bash
# Build
mvn clean package -DskipTests

# Test
mvn test

# Run
java -jar target/azaala-vcs-2.0.0.jar

# Debug
mvn clean package && mvn exec:java -Dexec.mainClass="com.azaala.vcs.Main"

# Check style
mvn clean compile checkstyle:check

# Generate documentation
mvn javadoc:javadoc

# Find unused code
mvn dependency:analyze

# Update dependencies
mvn versions:display-dependency-updates
```

---

This quick start guide should help developers get productive quickly. For more detailed information, refer to the comprehensive documentation in the `/docs` folder.

