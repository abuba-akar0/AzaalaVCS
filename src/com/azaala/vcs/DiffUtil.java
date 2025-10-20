package com.azaala.vcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for comparing commits and generating diff reports.
 */
public class DiffUtil {

    /**
     * Compares two commits and generates a detailed diff.
     *
     * @param commit1 First commit
     * @param commit2 Second commit
     * @return List of diff lines
     */
    public List<String> compareCommits(Commit commit1, Commit commit2) {
        List<String> diffLines = new ArrayList<>();

        if (commit1 == null || commit2 == null) {
            diffLines.add("Cannot compare: one or both commits are null");
            return diffLines;
        }

        // Compare file lists
        Set<String> files1 = new HashSet<>(commit1.getChangedFiles());
        Set<String> files2 = new HashSet<>(commit2.getChangedFiles());

        Set<String> allFiles = new HashSet<>(files1);
        allFiles.addAll(files2);

        if (allFiles.isEmpty()) {
            diffLines.add("No files in either commit");
            return diffLines;
        }

        // Categorize files
        List<String> addedFiles = new ArrayList<>();
        List<String> removedFiles = new ArrayList<>();
        List<String> commonFiles = new ArrayList<>();

        for (String file : allFiles) {
            if (files1.contains(file) && files2.contains(file)) {
                commonFiles.add(file);
            } else if (files2.contains(file) && !files1.contains(file)) {
                addedFiles.add(file);
            } else if (files1.contains(file) && !files2.contains(file)) {
                removedFiles.add(file);
            }
        }

        // Generate summary
        diffLines.add("=== File Changes Summary ===");
        diffLines.add("Added files: " + addedFiles.size());
        diffLines.add("Removed files: " + removedFiles.size());
        diffLines.add("Common files: " + commonFiles.size());
        diffLines.add("");

        // Detail changes
        if (!addedFiles.isEmpty()) {
            diffLines.add("Added Files:");
            Collections.sort(addedFiles);
            for (String file : addedFiles) {
                diffLines.add("  + " + file);
            }
            diffLines.add("");
        }

        if (!removedFiles.isEmpty()) {
            diffLines.add("Removed Files:");
            Collections.sort(removedFiles);
            for (String file : removedFiles) {
                diffLines.add("  - " + file);
            }
            diffLines.add("");
        }

        if (!commonFiles.isEmpty()) {
            diffLines.add("Unchanged Files:");
            Collections.sort(commonFiles);
            for (String file : commonFiles) {
                diffLines.add("  = " + file);
            }
        }

        return diffLines;
    }

    /**
     * Gets added lines between two text files.
     *
     * @param oldFile First file
     * @param newFile Second file
     * @return List of added lines
     */
    public List<String> getAddedLines(File oldFile, File newFile) {
        List<String> addedLines = new ArrayList<>();

        if (oldFile == null || newFile == null) {
            return addedLines;
        }

        if (!oldFile.exists() || !newFile.exists()) {
            return addedLines;
        }

        try {
            // Read both files into memory
            Set<String> oldLines = new HashSet<>(readLines(oldFile));
            List<String> newLines = readLines(newFile);

            // Check each line in new file
            for (String line : newLines) {
                if (!oldLines.contains(line)) {
                    addedLines.add(line);
                }
            }

        } catch (IOException e) {
            System.err.println("Error comparing files: " + e.getMessage());
        }

        return addedLines;
    }

    /**
     * Gets removed lines between two text files.
     *
     * @param oldFile First file
     * @param newFile Second file
     * @return List of removed lines
     */
    public List<String> getRemovedLines(File oldFile, File newFile) {
        List<String> removedLines = new ArrayList<>();

        if (oldFile == null || newFile == null) {
            return removedLines;
        }

        if (!oldFile.exists() || !newFile.exists()) {
            return removedLines;
        }

        try {
            // Read both files into memory
            List<String> oldLines = readLines(oldFile);
            Set<String> newLines = new HashSet<>(readLines(newFile));

            // Check each line in old file
            for (String line : oldLines) {
                if (!newLines.contains(line)) {
                    removedLines.add(line);
                }
            }

        } catch (IOException e) {
            System.err.println("Error comparing files: " + e.getMessage());
        }

        return removedLines;
    }

    /**
     * Gets modified files from a list of staged files.
     *
     * @param stagedFiles List of staged files
     * @return List of modified files
     */
    public List<String> getModifiedFiles(List<File> stagedFiles) {
        List<String> modifiedFiles = new ArrayList<>();

        if (stagedFiles == null || stagedFiles.isEmpty()) {
            return modifiedFiles;
        }

        for (File file : stagedFiles) {
            if (file.exists() && file.isFile()) {
                modifiedFiles.add(file.getPath());
            }
        }

        return modifiedFiles;
    }

    /**
     * Read all lines from a file.
     *
     * @param file File to read
     * @return List of lines
     * @throws IOException If an I/O error occurs
     */
    private List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Generates a quick diff summary.
     *
     * @param commit1 First commit
     * @param commit2 Second commit
     * @return Summary string
     */
    public String generateQuickDiff(Commit commit1, Commit commit2) {
        if (commit1 == null || commit2 == null) {
            return "Cannot compare: invalid commits";
        }

        int files1Count = commit1.getFileCount();
        int files2Count = commit2.getFileCount();
        int difference = files2Count - files1Count;

        if (difference > 0) {
            return "+" + difference + " files added";
        } else if (difference < 0) {
            return Math.abs(difference) + " files removed";
        } else {
            return "Same number of files (" + files1Count + ")";
        }
    }

    /**
     * Generates a unified diff format between two files.
     *
     * @param oldFile First file
     * @param newFile Second file
     * @return Unified diff as a string
     */
    public String getUnifiedDiff(File oldFile, File newFile) {
        if (oldFile == null || newFile == null) {
            return "Error: One or both files are null";
        }

        if (!oldFile.exists() || !newFile.exists()) {
            return "Error: One or both files don't exist";
        }

        StringBuilder diff = new StringBuilder();
        diff.append("--- ").append(oldFile.getPath()).append("\n");
        diff.append("+++ ").append(newFile.getPath()).append("\n");

        try {
            List<String> oldLines = readLines(oldFile);
            List<String> newLines = readLines(newFile);

            List<String> addedLines = getAddedLines(oldFile, newFile);
            List<String> removedLines = getRemovedLines(oldFile, newFile);

            diff.append("@@ -1,").append(oldLines.size())
                    .append(" +1,").append(newLines.size())
                    .append(" @@\n");

            // Add removed lines with - prefix
            for (String line : removedLines) {
                diff.append("- ").append(line).append("\n");
            }

            // Add added lines with + prefix
            for (String line : addedLines) {
                diff.append("+ ").append(line).append("\n");
            }

            // Show context lines
            Set<String> contextLines = new HashSet<>(oldLines);
            contextLines.removeAll(removedLines);

            for (String line : contextLines) {
                if (newLines.contains(line)) {
                    diff.append("  ").append(line).append("\n");
                }
            }

        } catch (IOException e) {
            diff.append("Error generating diff: ").append(e.getMessage());
        }

        return diff.toString();
    }
}
