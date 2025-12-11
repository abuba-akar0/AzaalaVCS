package com.azaala.vcs;

import java.util.Arrays;
import java.util.List;

/**
 * Handles command parsing and execution for the VCS system.
 * Provides validation and routing for different VCS commands.
 */
public class CommandHandler {
    private final VCS vcs;

    // Valid commands
    private static final List<String> VALID_COMMANDS = Arrays.asList(
            "init", "add", "commit", "status", "log", "diff", "help", "version", "exit"
    );

    /**
     * Creates a new CommandHandler instance.
     */
    public CommandHandler() {
        this.vcs = new VCS();
    }

    /**
     * Creates a CommandHandler with a specific VCS instance.
     *
     * @param vcs The VCS instance to use
     */
    public CommandHandler(VCS vcs) {
        this.vcs = vcs != null ? vcs : new VCS();
    }

    /**
     * Executes a command with the given arguments.
     *
     * @param command The command to execute
     * @param args Command arguments
     * @return true if command executed successfully, false otherwise
     */
    public boolean executeCommand(String command, String[] args) {
        if (command == null || command.trim().isEmpty()) {
            System.err.println("Command cannot be empty");
            return false;
        }

        String normalizedCommand = command.trim().toLowerCase();

        if (!VALID_COMMANDS.contains(normalizedCommand)) {
            System.err.println("Unknown command: " + command);
            return false;
        }

        try {
            switch (normalizedCommand) {
                case "init":
                    return handleInit(args);
                case "add":
                    return handleAdd(args);
                case "commit":
                    return handleCommit(args);
                case "status":
                    return handleStatus(args);
                case "log":
                    return handleLog(args);
                case "diff":
                    return handleDiff(args);
                case "help":
                    return handleHelp(args);
                case "version":
                    return handleVersion(args);
                case "exit":
                    System.out.println("Exiting Azaala VCS. Goodbye!");
                    return true;
                default:
                    System.err.println("Command not implemented: " + command);
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Error executing command '" + command + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Handles the init command.
     */
    private boolean handleInit(String[] args) {
        String path = args.length > 0 ? args[0] : System.getProperty("user.dir");
        return vcs.initRepository(path);
    }

    /**
     * Handles the add command.
     */
    private boolean handleAdd(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: add <file-path>");
            return false;
        }
        return vcs.addFile(args[0]);
    }

    /**
     * Handles the commit command.
     */
    private boolean handleCommit(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: commit <message>");
            return false;
        }

        // Join all arguments as the commit message
        String message = String.join(" ", args);
        String commitId = vcs.commit(message);
        return commitId != null && !commitId.isEmpty();
    }

    /**
     * Handles the status command.
     */
    private boolean handleStatus(String[] args) {
        List<String> status = vcs.getStatus();
        status.forEach(System.out::println);
        return true;
    }

    /**
     * Handles the log command.
     */
    private boolean handleLog(String[] args) {
        List<String> log = vcs.log();
        log.forEach(System.out::println);
        return true;
    }

    /**
     * Handles the diff command.
     */
    private boolean handleDiff(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: diff <commit-id1> <commit-id2>");
            return false;
        }

        List<String> diff = vcs.diff(args[0], args[1]);
        diff.forEach(System.out::println);
        return true;
    }

    /**
     * Handles the help command.
     */
    private boolean handleHelp(String[] args) {
        printHelp();
        return true;
    }

    /**
     * Handles the version command.
     */
    private boolean handleVersion(String[] args) {
        System.out.println("Azaala VCS version 1.0.0");
        return true;
    }

    /**
     * Prints help information.
     */
    private void printHelp() {
        System.out.println("Azaala VCS - Available Commands:");
        System.out.println("  init [path]              Initialize repository");
        System.out.println("  add <file>               Add file to staging");
        System.out.println("  commit <message>         Create commit");
        System.out.println("  status                   Show status");
        System.out.println("  log                      Show commit history");
        System.out.println("  diff <id1> <id2>         Compare commits");
        System.out.println("  help                     Show this help");
        System.out.println("  version                  Show version");
    }

    /**
     * Validates command arguments.
     */
    public boolean validateCommand(String command, String[] args) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }

        String normalizedCommand = command.trim().toLowerCase();
        return VALID_COMMANDS.contains(normalizedCommand);
    }

    /**
     * Gets the VCS instance.
     */
    public VCS getVCS() {
        return vcs;
    }
}
