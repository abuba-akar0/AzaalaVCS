# Azaala VCS Design Document

## Architecture Overview

Azaala VCS is a simple version control system designed with a clean, modular architecture. It follows object-oriented principles and separates concerns into distinct components that interact through well-defined interfaces.

## Key Components

### 1. Core Classes

#### Main
- Entry point for the application
- Provides a menu-driven interface for user interaction
- Delegates commands to the CommandHandler

#### VCS
- Acts as the facade for all version control operations
- Coordinates interactions between components
- Provides high-level methods for version control operations

#### Repository
- Manages the repository structure
- Handles file storage and retrieval
- Maintains references to commits and staged files

#### Commit
- Represents a snapshot of files at a specific point in time
- Contains metadata about the changes
- Provides methods for accessing commit information

### 2. Utility Classes

#### FileHandler
- Handles file I/O operations
- Provides methods for reading, writing, and copying files
- Abstracts away low-level file system interactions

#### DiffUtil
- Compares files and generates difference reports
- Identifies added, removed, and modified lines
- Formats differences for display

#### SummaryGenerator
- Creates human-readable summaries of changes
- Analyzes diffs to extract meaningful information
- Categorizes changes by type and importance

#### Utils
- Provides general utility methods
- Handles timestamps, hashing, and string formatting
- Implements common helper functions

### 3. User Interface

#### CommandHandler
- Parses and processes user commands
- Translates commands into VCS operations
- Returns results to the user interface

## Data Flow

1. User inputs a command through the menu interface
2. Main delegates to CommandHandler
3. CommandHandler processes the command and calls appropriate VCS methods
4. VCS coordinates between Repository, FileHandler, and other components
5. Results flow back up to the user interface for display

## File Structure

```
Azaala_VCS/
├── src/
│   └── com/
│       └── azaala/
│           └── vcs/
│               ├── Main.java
│               ├── VCS.java
│               ├── Repository.java
│               ├── Commit.java
│               ├── FileHandler.java
│               ├── DiffUtil.java
│               ├── SummaryGenerator.java
│               ├── CommandHandler.java
│               └── Utils.java
├── data/
│   ├── commits/
│   └── index/
├── docs/
└── tests/
```

## Data Storage

### Repository Structure
- `/data/commits/` - Stores commit snapshots and metadata
- `/data/index/` - Stores staged files and repository state
  - `staged_files.txt` - List of files staged for commit
  - `head.txt` - Reference to the latest commit

### Commit Structure
- `/data/commits/commit_<id>/`
  - `metadata.txt` - Contains commit message, timestamp, and other metadata
  - `/snapshot/` - Contains copies of files at commit time

## Key Algorithms

### Diff Generation
1. Read both file versions line by line
2. Compare lines to identify additions and removals
3. Format the differences for display

### Commit Process
1. Create a new commit ID based on timestamp and metadata
2. Copy staged files to a snapshot directory
3. Write commit metadata
4. Update HEAD reference

### Summary Generation
1. Compare current and previous commits
2. Identify added, modified, and removed files
3. Apply heuristics to determine important changes
4. Generate a human-readable summary

## Design Principles

- **Separation of Concerns**: Each class has a specific responsibility
- **Encapsulation**: Implementation details are hidden behind interfaces
- **Error Handling**: Robust error checking and reporting
- **Testability**: Components designed to be easily tested

## Future Enhancements

- **Branching and Merging**: Support for multiple branches and merging
- **Remote Repositories**: Push and pull functionality
- **Conflict Resolution**: Tools for resolving merge conflicts
- **GUI Interface**: Graphical user interface for easier interaction

