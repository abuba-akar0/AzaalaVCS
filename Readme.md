# 🚀 Azaala VCS - Version Control System

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
![Java](https://img.shields.io/badge/java-11%2B-orange.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Status](https://img.shields.io/badge/status-production-brightgreen.svg)

A lightweight, educational version control system built with Java featuring both a modern GUI and command-line interface.

[Documentation](#documentation) • [Features](#features) • [Quick Start](#quick-start) • [Architecture](#architecture)

</div>

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Quick Start](#quick-start)
4. [Architecture](#architecture)
5. [Components](#components)
6. [Usage Guide](#usage-guide)
7. [Database Integration](#database-integration)
8. [API Reference](#api-reference)
9. [Development](#development)
10. [Contributing](#contributing)
11. [License](#license)

---

## 📖 Project Overview

**Azaala VCS** is a sophisticated yet easy-to-understand version control system designed for educational purposes. It implements core VCS concepts similar to Git with a simplified, extensible architecture. Built with Java 11+, it features:

- 🖥️ **Modern Swing-based GUI** with intuitive dashboard
- ⌨️ **Powerful Command-Line Interface** for automation
- 💾 **MySQL Database Backend** with connection pooling
- 🔄 **Asynchronous Operations** using SwingWorker threads
- 📊 **Advanced Diff Generation** with detailed change tracking
- 🤖 **Intelligent Summary Generation** for commits
- 🎨 **Theme Support** with customizable UI

---

## ✨ Features

### Core VCS Features
- ✅ **Repository Management** - Initialize, configure, and manage repositories
- ✅ **File Staging** - Add files to staging area before committing
- ✅ **Commits** - Create snapshots with descriptive messages and summaries
- ✅ **Commit History** - View complete history with timestamps
- ✅ **Diff Analysis** - Compare files between any two commits
- ✅ **Status Tracking** - Monitor repository and file status

### Advanced Features
- ✅ **Automatic Summary Generation** - AI-like analysis of changes
- ✅ **Database Persistence** - Store all data in MySQL
- ✅ **Connection Pooling** - Efficient database access with HikariCP
- ✅ **Multi-threaded Operations** - Non-blocking UI with SwingWorker
- ✅ **Batch Operations** - Add multiple files at once
- ✅ **Preferences Management** - Customize system behavior

### GUI Features
- ✅ **Dashboard** - Overview of repository status
- ✅ **History Panel** - Browse commit history
- ✅ **Diff Viewer** - Visual file comparison
- ✅ **Status Panel** - Monitor staged and unstaged changes
- ✅ **Preferences Dialog** - Configure application settings
- ✅ **Settings Panel** - Manage database and repository options

---

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- MySQL 8.0+
- Maven 3.6+

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/azaala/azaala-vcs.git
cd azaala-vcs

# 2. Configure database
cp src/main/resources/db.properties.example src/main/resources/db.properties
# Edit db.properties with your MySQL credentials

# 3. Build the project
mvn clean package

# 4. Run the application
# GUI Mode (Default)
java -jar target/azaala-vcs-2.0.0.jar

# Console Mode
java -jar target/azaala-vcs-2.0.0.jar -c

# Help
java -jar target/azaala-vcs-2.0.0.jar -h
```

### Basic Workflow

```bash
# 1. Initialize a repository
java -jar azaala.jar init /path/to/repo

# 2. Add files to staging area
java -jar azaala.jar add src/Main.java

# 3. Commit changes
java -jar azaala.jar commit "Initial commit"

# 4. View history
java -jar azaala.jar log

# 5. Compare commits
java -jar azaala.jar diff <commit1_id> <commit2_id>
```

---

## 🏗️ Architecture

### System Architecture Diagram

```
┌────────────────────��────────────────────────────────────┐
│                    User Interface Layer                 │
│  ┌──────────────────────────────────────────────────┐   │
│  │          GUI Dashboard (Swing/JavaFX)            │   │
│  │  ├─ OverviewPanel    ├─ HistoryPanel            │   │
│  │  ├─ StatusPanel      ├─ DiffPanel               │   │
│  │  ├─ SettingsPanel    ├─ PreferencesDialog       │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                 Command Handler & Core VCS              │
│  ┌──────────────────────────────────────────────────┐   │
│  │            CommandHandler (CLI Router)           │   │
│  │  ├─ init      ├─ add        ├─ commit            │   │
│  │  ├─ log       ├─ diff       ├─ status            │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │              VCS (Core Facade)                   │   │
│  │  Repository   Commit   FileHandler               │   │
│  │  DiffUtil     SummaryGenerator   Utils           │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│         Asynchronous Worker & Threading Layer           │
│  ┌──────────────────────────────────────────────────┐   │
│  │    BaseVCSWorker (Abstract SwingWorker Base)     │   │
│  │  ├─ RepositoryInitWorker    ├─ AddFileWorker    │   │
│  │  ├─ CommitWorker            ├─ DiffWorker       │   │
│  │  ├─ HistoryLoadWorker       ├─ StatusLoadWorker │   │
│  │  ├─ AddAllFilesWorker                           │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│             Persistence & Database Layer                │
│  ┌──────────────────────────────────────────────────┐   │
│  │      DatabaseManager & Connection Management    │   │
│  │  ├─ DatabaseConfig    ├─ ConnectionPool         │   │
│  │  ├─ DAO Package       ├─ Models Package         │   │
│  │  ├─ DatabaseException                           │   │
│  └──────────────────────────────────────────────────┘   │
│                          ↓                               │
│           ┌────────────────────────────┐                │
│           │  MySQL 8.0 Database        │                │
│           │  ├─ Repositories           │                │
│           │  ├─ Commits                │                │
│           │  ├─ Commit Files           │                │
│           │  ├─ Commit Diffs           │                │
│           │  └─ Settings               │                │
│           └────────────────────────────┘                │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│            File System & Data Storage Layer             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Repository Data Directory Structure             │   │
│  │  data/                                           │   │
│  │  ├─ commits/        (Commit snapshots)           │   │
│  │  ├─ index/          (Staging area)               │   │
│  │  ├─ commits.log     (History)                    │   │
│  │  └─ config.txt      (Repository config)          │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### Design Patterns Used

| Pattern | Usage | Benefits |
|---------|-------|----------|
| **Facade** | VCS class acts as unified interface | Simplifies complex subsystems |
| **Singleton** | DatabaseManager, ConnectionPool, UITheme | Single instance control |
| **Factory** | WorkerFactory creates SwingWorker instances | Decouples worker creation |
| **Builder** | PreferencesDialog configuration | Complex object construction |
| **Observer** | ProgressListener for async updates | Loose coupling between components |
| **Command** | CommandHandler for routing | Extensible command structure |
| **Data Access Object** | DAO pattern in persistence layer | Data abstraction |

---

## 🔧 Components

Azaala VCS is modular with clear separation of concerns. Below is detailed documentation of each major component.

### Core Components

#### 1. **VCS.java** - Core Facade
- **Package**: `com.azaala.vcs`
- **Responsibility**: Main entry point for all VCS operations
- **Key Methods**:
  - `initRepository(String path)` - Initialize new repository
  - `addFile(String filePath)` - Stage a file
  - `commit(String message)` - Create a commit
  - `getCommitHistory()` - Retrieve all commits
  - `getRepository()` - Get current repository
- **Dependencies**: Repository, FileHandler, DiffUtil, SummaryGenerator

#### 2. **Repository.java** - Repository Management
- **Package**: `com.azaala.vcs`
- **Responsibility**: Represents and manages a repository
- **Key Methods**:
  - `isInitialized()` - Check if repository is ready
  - `createCommit(Commit)` - Save commit to repository
  - `loadCommits()` - Load all commits from storage
  - `getPath()` - Get repository path
  - `getName()` - Get repository name
- **Properties**:
  - `repositoryPath` - Full path to repository
  - `commits` - List of all commits
  - `repoId` - Database ID for persistence

#### 3. **Commit.java** - Commit Model
- **Package**: `com.azaala.vcs`
- **Responsibility**: Represents a single commit snapshot
- **Key Methods**:
  - `generateUniqueId()` - Create commit ID
  - `generateDefaultSummary()` - Auto-generate summary
  - `getChangedFiles()` - Get list of changed files
  - `getFileCount()` - Count of files in commit
- **Properties**:
  - `commitId` - Unique identifier
  - `message` - Commit message
  - `summary` - AI-generated summary
  - `timestamp` - Creation time
  - `changedFiles` - Files in this commit

#### 4. **FileHandler.java** - File Operations
- **Package**: `com.azaala.vcs`
- **Responsibility**: Handles all file I/O operations
- **Key Methods**:
  - `readFile(String filePath)` - Read file content
  - `writeFile(String filePath, String content)` - Write to file
  - `copyToIndex(String source, String index)` - Copy file to staging
  - `copyFile(String source, String target)` - Copy between locations
  - `copyDirectory(String source, String target)` - Copy entire directory
  - `getStagedFiles()` - Get files in staging area
- **Error Handling**: Graceful exceptions with proper logging

#### 5. **DiffUtil.java** - Diff Generation
- **Package**: `com.azaala.vcs`
- **Responsibility**: Advanced diff analysis and comparison
- **Key Methods**:
  - `generateDetailedDiff(Commit, Commit, String)` - Line-by-line comparison
  - `compareFiles(String file1, String file2)` - File content comparison
  - `categorizeChanges(Set, Set, Set)` - Categorize added/removed/modified
- **Features**:
  - Detailed diff output with statistics
  - File categorization
  - Visual formatting with unicode boxes

#### 6. **SummaryGenerator.java** - Summary Generation
- **Package**: `com.azaala.vcs`
- **Responsibility**: AI-like analysis and summary creation
- **Key Methods**:
  - `generateSummary(Commit, Commit)` - Create summary text
  - `countFilesByType(List, String...)` - Classify files
  - `detectFunctionChanges(String)` - Find new functions
  - `extractTODOs(String)` - Find TODO/FIXME comments
- **Patterns Used**:
  - Regular expressions for detection
  - File extension analysis
  - Heuristic-based categorization

#### 7. **CommandHandler.java** - Command Router
- **Package**: `com.azaala.vcs`
- **Responsibility**: Parse and route CLI commands
- **Supported Commands**:
  - `init` - Initialize repository
  - `add` - Add file to staging
  - `commit` - Create commit
  - `log` - Show history
  - `diff` - Compare commits
  - `status` - Show repository status
  - `help` - Display help
  - `version` - Show version
  - `exit` - Exit application

#### 8. **Utils.java** - Utility Functions
- **Package**: `com.azaala.vcs`
- **Responsibility**: Common helper functions
- **Key Methods**:
  - `generateUniqueId()` - Create unique identifiers
  - `isValidPath(String)` - Validate file paths
  - `isFileWithinDirectory(File, File)` - Check file boundaries
  - `getFileName(String)` - Extract filename
  - `calculateFileHash(String)` - SHA-256 hash
  - `formatTimestamp(LocalDateTime)` - Format dates
- **Security**: Path validation and sanitization

### GUI Components

#### 9. **Dashboard.java** - Main GUI Window
- **Package**: `com.azaala.vcs.gui`
- **Responsibility**: Main application frame and layout
- **Features**:
  - Tabbed interface with multiple panels
  - Menu bar with all operations
  - Toolbar with quick actions
  - Status bar showing current state
  - Theme application

#### 10. **OverviewPanel.java** - Repository Overview
- **Package**: `com.azaala.vcs.gui`
- **Displays**:
  - Current repository information
  - Repository statistics
  - Latest commits
  - Quick action buttons

#### 11. **HistoryPanel.java** - Commit History
- **Package**: `com.azaala.vcs.gui`
- **Features**:
  - List all commits with details
  - Search and filter commits
  - Show commit details on selection
  - Display changed files per commit

#### 12. **StatusPanel.java** - Repository Status
- **Package**: `com.azaala.vcs.gui`
- **Shows**:
  - Staged files list
  - Unstaged files list
  - File status indicators
  - Staging/Unstaging controls

#### 13. **DiffPanel.java** - Diff Viewer
- **Package**: `com.azaala.vcs.gui`
- **Features**:
  - Select two commits to compare
  - Display detailed diff results
  - Show statistics
  - File change visualization

#### 14. **SettingsPanel.java** - Settings Management
- **Package**: `com.azaala.vcs.gui`
- **Options**:
  - Database configuration
  - Repository settings
  - Theme preferences
  - Auto-save options

#### 15. **PreferencesDialog.java** - User Preferences
- **Package**: `com.azaala.vcs.gui`
- **Preferences**:
  - UI theme selection
  - Database connection settings
  - Auto-initialization options
  - Default commit message templates

#### 16. **UITheme.java** - Theme Management
- **Package**: `com.azaala.vcs.gui`
- **Features**:
  - Centralized color scheme
  - Font management
  - Component padding constants
  - Dynamic theme application

### Async Components

#### 17. **BaseVCSWorker.java** - Abstract Worker Base
- **Package**: `com.azaala.vcs.async`
- **Extends**: `SwingWorker<T, String>`
- **Responsibility**: Base class for all async operations
- **Key Methods**:
  - `doInBackground()` - Async computation (abstract)
  - `done()` - Handle completion
  - `process(List<String>)` - Update progress
  - `publishProgress(String, int)` - Notify UI
  - `onSuccess(T)`, `onError(Exception)` - Success/failure handlers

#### 18. **Worker Implementations**
- **RepositoryInitWorker** - Async repository initialization
- **AddFileWorker** - Async file staging
- **AddAllFilesWorker** - Batch file addition
- **CommitWorker** - Async commit creation
- **DiffWorker** - Async diff generation
- **HistoryLoadWorker** - Async history loading
- **StatusLoadWorker** - Async status computation

#### 19. **WorkerFactory.java** - Worker Creation
- **Package**: `com.azaala.vcs.async`
- **Responsibility**: Factory pattern for worker instantiation
- **Methods**: Creates appropriate worker based on task type

#### 20. **ProgressListener.java** - Progress Callback
- **Package**: `com.azaala.vcs.async`
- **Interface**: Defines progress notification contract
- **Methods**:
  - `onProgress(String message, int progress)`
  - `onSuccess(String result)`
  - `onError(String message, Throwable error)`

### Persistence Components

#### 21. **DatabaseManager.java** - Database Management
- **Package**: `com.azaala.vcs.persistence`
- **Responsibility**: Database initialization and schema management
- **Key Methods**:
  - `initialize()` - Initialize database connection
  - `initializeSchema()` - Create tables and schema
  - `getInstance()` - Singleton access
- **Features**:
  - Automatic schema creation
  - Connection pool management
  - Error handling and recovery

#### 22. **DatabaseConfig.java** - Configuration
- **Package**: `com.azaala.vcs.persistence`
- **Responsibility**: Database configuration management
- **Properties**:
  - Host, port, database name
  - Username and password
  - Connection pool settings
  - Auto-initialization flag

#### 23. **ConnectionPool.java** - Connection Pooling
- **Package**: `com.azaala.vcs.persistence`
- **Technology**: HikariCP
- **Features**:
  - Connection pool management
  - Connection timeout handling
  - Connection validation
  - Resource cleanup

#### 24. **DatabaseException.java** - Custom Exception
- **Package**: `com.azaala.vcs.persistence`
- **Responsibility**: Specialized exception for database errors
- **Usage**: Better error handling and debugging

#### 25. **DAO Package** - Data Access Objects
- **Package**: `com.azaala.vcs.persistence.dao`
- **Responsibility**: Database operations for entities
- **Includes**:
  - Repository DAO
  - Commit DAO
  - File DAO
  - CRUD operations

#### 26. **Models Package** - Entity Models
- **Package**: `com.azaala.vcs.persistence.models`
- **Responsibility**: Database entity representations
- **Includes**:
  - Repository model
  - Commit model
  - File model
  - Settings model

---

## 📖 Usage Guide

### GUI Mode

#### Starting the Application
```bash
java -jar azaala-vcs-2.0.0.jar
```

#### Workflow Steps

1. **Initialize Repository**
   - Click "Init Repository" button
   - Select or create a directory
   - Wait for initialization to complete

2. **Add Files**
   - Go to Status Panel
   - Click "Add File" button
   - Select files from dialog
   - Files appear in staging area

3. **Commit Changes**
   - Review staged files
   - Enter commit message in dialog
   - System auto-generates summary
   - Click "Commit"

4. **View History**
   - Switch to History Panel
   - Browse all commits
   - Click commit to see details
   - View changed files list

5. **Compare Commits**
   - Go to Diff Panel
   - Select two commits to compare
   - View detailed diff results
   - See statistics and changes

### Command-Line Mode

#### Start Console Mode
```bash
java -jar azaala-vcs-2.0.0.jar -c
```

#### Interactive Menu
```
===== Azaala VCS - Version Control System =====
1. Init Repository
2. Add File
3. Add All Files
4. Commit Changes
5. Show Status
6. Show Log
7. Compare Commits (Diff)
8. Preferences
9. Exit

==============================================
Enter your choice (1-9):
```

#### Direct Command Execution
```bash
# Initialize
java -jar azaala-vcs-2.0.0.jar -c init /path/to/repo

# Add file
java -jar azaala-vcs-2.0.0.jar -c add /path/to/file.java

# Commit
java -jar azaala-vcs-2.0.0.jar -c commit "Commit message"

# Show log
java -jar azaala-vcs-2.0.0.jar -c log

# Show status
java -jar azaala-vcs-2.0.0.jar -c status

# Show diff
java -jar azaala-vcs-2.0.0.jar -c diff commit1_id commit2_id
```

---

## 💾 Database Integration

### Database Schema

The system uses MySQL with the following main tables:

```sql
-- Repositories table
CREATE TABLE repositories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  path VARCHAR(1024) NOT NULL UNIQUE,
  created_at TIMESTAMP,
  description TEXT
);

-- Commits table
CREATE TABLE commits (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  commit_id VARCHAR(64) NOT NULL UNIQUE,
  message VARCHAR(500) NOT NULL,
  summary TEXT,
  created_at TIMESTAMP,
  FOREIGN KEY (repo_id) REFERENCES repositories(id)
);

-- Commit files table
CREATE TABLE commit_files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  commit_id BIGINT NOT NULL,
  file_path VARCHAR(1024) NOT NULL,
  file_hash VARCHAR(64),
  FOREIGN KEY (commit_id) REFERENCES commits(id)
);

-- Settings table
CREATE TABLE settings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  repo_id BIGINT NOT NULL,
  key_name VARCHAR(255) NOT NULL,
  key_value TEXT,
  FOREIGN KEY (repo_id) REFERENCES repositories(id)
);
```

### Configuration

Create `src/main/resources/db.properties`:

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

### Connection Management

The system uses HikariCP for efficient connection pooling:

```java
// Get connection from pool
Connection conn = connectionPool.getConnection();

try {
    // Use connection
    Statement stmt = conn.createStatement();
    // ... execute queries
} finally {
    // Connection automatically returned to pool
    conn.close();
}
```

---

## 🔌 API Reference

### VCS Class Methods

```java
// Initialize repository
boolean initRepository(String path)

// File operations
boolean addFile(String filePath)
boolean addAllFiles(String directoryPath)
boolean removeFile(String filePath)

// Commit operations
boolean commit(String message)
String commitWithSummary(String message)

// Query operations
List<Commit> getCommitHistory()
Commit getCommitById(String commitId)
Repository getRepository()

// Diff operations
List<String> generateDiff(Commit commit1, Commit commit2)

// Status operations
List<String> getStagedFiles()
List<String> getUnstagedFiles()
String getRepositoryStatus()
```

### Repository Class Methods

```java
// Accessors
String getPath()
String getName()
LocalDateTime getCreatedAt()
List<Commit> getCommits()

// Repository operations
boolean isInitialized()
String createCommit(Commit commit)
void loadExistingCommits()

// Database integration
Long getRepoId()
void setRepoId(Long repoId)
```

### Commit Class Methods

```java
// Basic info
String getCommitId()
String getMessage()
LocalDateTime getTimestamp()

// File information
List<String> getChangedFiles()
int getFileCount()

// Summary
String getSummary()
void setSummary(String summary)

// Validation
void validateMessage(String message)
void validateChangedFiles(List<String> files)
```

---

## 🛠️ Development

### Building from Source

```bash
# Clone repository
git clone https://github.com/azaala/azaala-vcs.git
cd azaala-vcs

# Build with Maven
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Create executable JAR
mvn clean package -DskipTests
```

### Project Structure

```
src/
├── main/
│   ├── java/com/azaala/vcs/
│   │   ├── Main.java
│   │   ├── VCS.java
│   │   ├── Repository.java
│   │   ├── Commit.java
│   │   ├── FileHandler.java
│   │   ├── DiffUtil.java
│   │   ├── SummaryGenerator.java
│   │   ├── CommandHandler.java
│   │   ├── Utils.java
│   │   ├── async/
│   │   │   ├── BaseVCSWorker.java
│   │   │   ├── [Worker implementations...]
│   │   │   └── WorkerFactory.java
│   │   ├── gui/
│   │   │   ├── Dashboard.java
│   │   │   ├── [Panel implementations...]
│   │   │   ├── UITheme.java
│   │   │   └── PreferencesManager.java
│   │   └── persistence/
│   │       ├── DatabaseManager.java
│   │       ├── DatabaseConfig.java
│   │       ├── ConnectionPool.java
│   │       ├── DatabaseException.java
│   │       ├── dao/
│   │       └── models/
│   └── resources/
│       ├── db.properties
│       └── schema.sql
└── test/
    ├── java/com/azaala/vcs/
    └── resources/
```

### Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| MySQL Connector | 8.0.33 | Database connection |
| HikariCP | 5.0.1 | Connection pooling |
| SLF4J | 1.7.36 | Logging framework |
| JUnit | 4.13.2 | Unit testing |

### Code Style Guidelines

- Follow Java naming conventions
- Use meaningful variable names
- Add Javadoc comments for public methods
- Keep methods under 30 lines when possible
- Use proper exception handling
- Validate input parameters

---

## 🤝 Contributing

### How to Contribute

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

### Reporting Issues

Please include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)
- Environment details (Java version, OS, etc.)

---

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for full details.

```
MIT License

Copyright (c) 2024 Azaala VCS

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 📞 Support & Contact

- **Documentation**: See `/docs` folder
- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions

---

<div align="center">

**Made with ❤️ by Azaala Team**

[Back to Top](#-azaala-vcs---version-control-system)

</div>

