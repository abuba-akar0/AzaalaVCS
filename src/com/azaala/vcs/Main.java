package com.azaala.vcs;

import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the Azaala Version Control System.
 * Provides a menu-driven interface for interacting with the VCS.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final CommandHandler commandHandler = new CommandHandler();

    private static final String VERSION = "1.0.0";
    private static final String APP_NAME = "Azaala VCS";

    /**
     * Main method that starts the application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Check if help is requested via command line
        if (args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) {
            printHelp();
            return;
        }

        // Check for version flag
        if (args.length > 0 && (args[0].equals("-v") || args[0].equals("--version"))) {
            printVersion();
            return;
        }

        // Check for direct command execution
        if (args.length > 0) {
            executeCommand(args);
            return;
        }

        try {
            runMainMenu();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Executes a command directly from command line arguments.
     *
     * @param args Command line arguments
     */
    private static void executeCommand(String[] args) {
        try {
            String command = args[0].toLowerCase();
            String[] commandArgs = new String[args.length - 1];
            System.arraycopy(args, 1, commandArgs, 0, args.length - 1);

            // Use commandHandler to execute commands instead of duplicating logic
            if (!commandHandler.executeCommand(command, commandArgs)) {
                System.err.println("Command failed: " + command);
                System.err.println("Use -h or --help for help information");
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }

    /**
     * Runs the main menu loop.
     */
    private static void runMainMenu() {
        boolean running = true;

        printWelcome();

        while (running) {
            displayMenu();
            int choice = getMenuChoice();

            switch (choice) {
                case 1:
                    initRepository();
                    break;
                case 2:
                    addFile();
                    break;
                case 3:
                    commitChanges();
                    break;
                case 4:
                    showStatus();
                    break;
                case 5:
                    showLog();
                    break;
                case 6:
                    compareCommits();
                    break;
                case 7:
                    showActivitySummary();
                    break;
                case 8:
                    System.out.println("Exiting " + APP_NAME + ". Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter a number between 1-8.");
                    break;
            }

            if (running) {
                pauseForUser();
            }
        }
    }

    /**
     * Prints welcome message.
     */
    private static void printWelcome() {
        System.out.println("============================================");
        System.out.println("Welcome to " + APP_NAME + " v" + VERSION + "!");
        System.out.println("Simple and Efficient Version Control System");
        System.out.println("============================================");
    }

    /**
     * Displays the main menu.
     */
    private static void displayMenu() {
        System.out.println("\n===== " + APP_NAME + " - Main Menu =====");
        System.out.println("1. Init Repository");
        System.out.println("2. Add File");
        System.out.println("3. Commit Changes");
        System.out.println("4. Show Status");
        System.out.println("5. Show Log");
        System.out.println("6. Compare Commits (Diff)");
        System.out.println("7. Activity Summary");
        System.out.println("8. Exit");
        System.out.println("==========================================");
        System.out.print("Enter your choice (1-8): ");
    }

    /**
     * Gets the user's menu choice with validation.
     *
     * @return Selected menu option as an integer, -1 if invalid
     */
    private static int getMenuChoice() {
        try {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Handles repository initialization with error handling.
     */
    private static void initRepository() {
        System.out.println("\n--- Initialize Repository ---");
        System.out.print("Enter repository path (leave blank for current directory): ");
        String path = scanner.nextLine().trim();

        if (path.isEmpty()) {
            path = System.getProperty("user.dir");
        }

        executeInitCommand(path);
    }

    /**
     * Executes the init command.
     *
     * @param path Repository path
     */
    private static void executeInitCommand(String path) {
        try {
            System.out.println("Initializing repository at: " + path);
            if (commandHandler.getVCS().initRepository(path)) {
                System.out.println("✓ Repository initialized successfully!");
                System.out.println("  Repository path: " + path);
                System.out.println("  You can now add files and create commits.");
            } else {
                System.out.println("✗ Failed to initialize repository.");
                System.out.println("  Please check if:");
                System.out.println("  - The path is valid and writable");
                System.out.println("  - A repository doesn't already exist at this location");
            }
        } catch (Exception e) {
            System.out.println("✗ Error initializing repository: " + e.getMessage());
        }
    }

    /**
     * Handles adding a file to the repository with validation.
     */
    private static void addFile() {
        System.out.println("\n--- Add File to Repository ---");
        System.out.print("Enter file path: ");
        String filePath = scanner.nextLine().trim();

        if (filePath.isEmpty()) {
            System.out.println("✗ File path cannot be empty.");
            return;
        }

        executeAddCommand(filePath);
    }

    /**
     * Executes the add command.
     *
     * @param filePath File path to add
     */
    private static void executeAddCommand(String filePath) {
        try {
            if (commandHandler.getVCS().addFile(filePath)) {
                System.out.println("✓ File added successfully: " + filePath);
                System.out.println("  Use 'commit' to save this change.");
            } else {
                System.out.println("✗ Failed to add file: " + filePath);
                System.out.println("  Please check if:");
                System.out.println("  - The file exists and is readable");
                System.out.println("  - The repository is initialized");
                System.out.println("  - The file is within the repository boundaries");
            }
        } catch (Exception e) {
            System.out.println("✗ Error adding file: " + e.getMessage());
        }
    }

    /**
     * Handles committing changes with validation.
     */
    private static void commitChanges() {
        System.out.println("\n--- Commit Changes ---");
        System.out.print("Enter commit message: ");
        String message = scanner.nextLine().trim();

        if (message.isEmpty()) {
            System.out.println("✗ Commit message cannot be empty.");
            return;
        }

        executeCommitCommand(message);
    }

    /**
     * Executes the commit command.
     *
     * @param message Commit message
     */
    private static void executeCommitCommand(String message) {
        try {
            String commitId = commandHandler.getVCS().commit(message);
            if (commitId != null && !commitId.isEmpty()) {
                System.out.println("✓ Changes committed successfully!");
                System.out.println("  Commit ID: " + commitId);
                System.out.println("  Message: " + message);
                System.out.println("  Use 'log' to view commit history.");
            } else {
                System.out.println("✗ Failed to commit changes.");
                System.out.println("  Please check if:");
                System.out.println("  - Files are staged for commit");
                System.out.println("  - The repository is initialized");
                System.out.println("  - The commit message is valid");
            }
        } catch (Exception e) {
            System.out.println("✗ Error committing changes: " + e.getMessage());
        }
    }

    /**
     * Shows the repository status.
     */
    private static void showStatus() {
        System.out.println("\n--- Repository Status ---");
        executeStatusCommand();
    }

    /**
     * Executes the status command.
     */
    private static void executeStatusCommand() {
        try {
            List<String> statusLines = commandHandler.getVCS().getStatus();

            if (statusLines == null || statusLines.isEmpty()) {
                System.out.println("No status information available.");
            } else {
                for (String line : statusLines) {
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving status: " + e.getMessage());
        }
    }

    /**
     * Displays the commit log with error handling.
     */
    private static void showLog() {
        System.out.println("\n--- Commit Log ---");
        executeLogCommand();
    }

    /**
     * Executes the log command.
     */
    private static void executeLogCommand() {
        try {
            List<String> logEntries = commandHandler.getVCS().log();

            if (logEntries == null || logEntries.isEmpty()) {
                System.out.println("No commits found in repository.");
                System.out.println("Use 'init' to initialize a repository and 'add' + 'commit' to create commits.");
            } else {
                for (String entry : logEntries) {
                    System.out.println(entry);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Error retrieving log: " + e.getMessage());
        }
    }

    /**
     * Compares two commits with validation.
     */
    private static void compareCommits() {
        System.out.println("\n--- Compare Commits ---");

        System.out.print("Enter first commit ID: ");
        String commitId1 = scanner.nextLine().trim();

        System.out.print("Enter second commit ID: ");
        String commitId2 = scanner.nextLine().trim();

        if (commitId1.isEmpty() || commitId2.isEmpty()) {
            System.out.println("✗ Both commit IDs must be provided.");
            return;
        }

        if (commitId1.equals(commitId2)) {
            System.out.println("✗ Commit IDs are identical. No differences to show.");
            return;
        }

        executeDiffCommand(commitId1, commitId2);
    }

    /**
     * Executes the diff command.
     *
     * @param commitId1 First commit ID
     * @param commitId2 Second commit ID
     */
    private static void executeDiffCommand(String commitId1, String commitId2) {
        try {
            List<String> differences = commandHandler.getVCS().diff(commitId1, commitId2);

            if (differences == null || differences.isEmpty()) {
                System.out.println("No differences found between commits.");
            } else {
                System.out.println("\nDifferences between " + commitId1 + " and " + commitId2 + ":");
                System.out.println("========================================");
                for (String diff : differences) {
                    System.out.println(diff);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Error comparing commits: " + e.getMessage());
        }
    }

    /**
     * Shows activity summary.
     */
    private static void showActivitySummary() {
        System.out.println("\n--- Activity Summary ---");
        System.out.print("Enter number of recent commits to include (default: 5): ");
        String input = scanner.nextLine().trim();

        int limit = 5; // default
        if (!input.isEmpty()) {
            try {
                limit = Integer.parseInt(input);
                if (limit <= 0) {
                    System.out.println("Using default limit of 5 commits.");
                    limit = 5;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Using default limit of 5 commits.");
            }
        }

        try {
            String summary = commandHandler.getVCS().getActivitySummary(limit);
            System.out.println(summary);
        } catch (Exception e) {
            System.out.println("✗ Error generating activity summary: " + e.getMessage());
        }
    }

    /**
     * Pauses execution and waits for user input.
     */
    private static void pauseForUser() {
        System.out.println("\nPress Enter to continue...");
        try {
            scanner.nextLine();
        } catch (Exception e) {
            // Ignore any input errors during pause
        }
    }

    /**
     * Prints help information for using the application.
     */
    private static void printHelp() {
        System.out.println(APP_NAME + " v" + VERSION + " - Simple Version Control System");
        System.out.println("Usage: java -jar azaala-vcs.jar [command] [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  init [path]              Initialize a new repository (default: current directory)");
        System.out.println("  add <file-path>          Add file to staging area");
        System.out.println("  commit <message>         Commit staged changes with message");
        System.out.println("  status                   Show repository status");
        System.out.println("  log                      Display commit history");
        System.out.println("  diff <id1> <id2>         Show differences between two commits");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -h, --help               Show this help message");
        System.out.println("  -v, --version            Show version information");
        System.out.println();
        System.out.println("Interactive Mode:");
        System.out.println("  Run without arguments to enter interactive menu mode");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar azaala-vcs.jar init");
        System.out.println("  java -jar azaala-vcs.jar add myfile.txt");
        System.out.println("  java -jar azaala-vcs.jar commit \"Initial commit\"");
        System.out.println("  java -jar azaala-vcs.jar log");
        System.out.println();
        System.out.println("For more information, visit: https://github.com/azaala/vcs");
    }

    /**
     * Prints version information.
     */
    private static void printVersion() {
        System.out.println(APP_NAME + " version " + VERSION);
        System.out.println("Copyright (c) 2024 Azaala. All rights reserved.");
        System.out.println("Built with Java " + System.getProperty("java.version"));
    }
}
