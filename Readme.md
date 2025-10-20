# Azaala VCS (Version Control System)

## Project Overview

Azaala VCS is a lightweight Java-based version control system designed for educational purposes. It implements core version control concepts similar to Git but with a simplified architecture. Azaala VCS provides basic file tracking, commits, and comparison capabilities through both an interactive menu-driven interface and command-line operations.

## Features

- **Repository Management**: Initialize and manage local repositories
- **File Tracking**: Add files to a staging area before committing
- **Commits**: Create snapshots of staged files with descriptive messages
- **History**: View commit logs with timestamps and messages
- **Diff Tool**: Compare files between commits to identify changes
- **Automatic Summary Generation**: AI-like analysis of changes to generate human-readable summaries
- **Simple User Interface**: Both menu-driven and command-line interfaces

## Repository Structure

```
Azaala_VCS/
├── src/                       # Source code
│   └── com/azaala/vcs/        # Main package
│       ├── Main.java          # Entry point
│       ├── VCS.java           # Core VCS functionality
│       ├── Repository.java    # Repository management
│       ├── Commit.java        # Commit representation
│       ├── FileHandler.java   # File I/O operations
│       ├── DiffUtil.java      # Diff generation
│       ├── SummaryGenerator.java # Commit summaries
│       ├── CommandHandler.java # Command processing
│       └── Utils.java         # Utility functions
├── data/                      # Repository data storage
│   ├── commits/               # Stores commit data
│   └── index/                 # Staging area and HEAD reference
└── docs/                      # Documentation
```

## Example Usage

### Interactive Menu

```
===== Azaala VCS - Version Control System =====
1. Init Repository
2. Add File
3. Commit Changes
4. Show Log
5. Compare Commits (Diff)
6. Exit
==============================================
Enter your choice (1-6):
```

### Command-line Usage

```bash
# Initialize a new repository
java -jar azaala.jar init

# Add a file to the staging area
java -jar azaala.jar add src/com/example/Main.java

# Commit changes with a message
java -jar azaala.jar commit "Initial commit"

# View commit history
java -jar azaala.jar log

# Compare two commits
java -jar azaala.jar diff commit_20231015_123456 commit_20231016_123456
```

## Midterm Feature: Automatic Commit Summary

The Automatic Commit Summary feature analyzes changes between commits and generates human-readable summaries. This feature:

1. **Analyzes Differences**: Compares files between the current and previous commits
2. **Detects Key Changes**: Identifies added/removed files, new functions, and resolved TODOs
3. **Categorizes Changes**: Labels files by type (code, docs, config, etc.)
4. **Generates Summary Text**: Creates a concise description of changes

### Example Output

```
Summary: Modified 3 files — 1 added, 2 changed.
Changes: 25 additions, 12 deletions.
Highlights: Added function 'calculateChecksum' in Utils.java. Updated README.md.
Tags: code, docs
```

### Implementation Details

The summary generation uses several heuristics:
- Line-by-line comparison to count changes
- Regular expressions to detect new functions and methods
- File extension analysis for categorization
- Todo/Fixme comment tracking

## Future Improvements

### GUI Interface
- Develop a JavaFX-based graphical interface
- Provide visual diff viewing
- File tree navigation and management

### JavaEE Web Interface
- Web-based repository management
- User authentication and authorization
- Online code viewing and history browsing

### Remote Repository Support
- Push/pull functionality to synchronize repositories
- Branch and merge capability
- Conflict resolution tools

### Advanced Features
- Integration with build systems
- Automated testing on commit
- Continuous integration hooks

## License

This project is available under the MIT License. See the LICENSE file for more details.

