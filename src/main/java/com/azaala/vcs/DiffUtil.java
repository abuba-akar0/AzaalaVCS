package com.azaala.vcs;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Advanced Diff Utility - Provides robust line-by-line and file-level comparison
 * Uses proper diff algorithms to show exact changes between file versions
 */
public class DiffUtil {

    /**
     * Compares two commits and generates detailed line-by-line diff
     *
     * @param commit1 First commit
     * @param commit2 Second commit
     * @param repoPath Repository path to access commit files
     * @return Detailed diff output
     */
    public List<String> generateDetailedDiff(Commit commit1, Commit commit2, String repoPath) {
        List<String> diffOutput = new ArrayList<>();

        if (commit1 == null || commit2 == null) {
            diffOutput.add("ERROR: Cannot compare null commits");
            return diffOutput;
        }

        // Get file lists
        Set<String> files1 = new HashSet<>(commit1.getChangedFiles());
        Set<String> files2 = new HashSet<>(commit2.getChangedFiles());

        // Categorize files
        Set<String> addedFiles = new HashSet<>(files2);
        addedFiles.removeAll(files1);

        Set<String> removedFiles = new HashSet<>(files1);
        removedFiles.removeAll(files2);

        Set<String> modifiedFiles = new HashSet<>(files1);
        modifiedFiles.retainAll(files2);

        // Header
        diffOutput.add("╔════════════════════════════════════════════════════════════════════════╗");
        diffOutput.add("║                    DETAILED COMMIT COMPARISON                          ║");
        diffOutput.add("╚════════════════════════════════════════════════════════════════════════╝");
        diffOutput.add("");

        // Commit info
        diffOutput.add("FROM COMMIT: " + commit1.getCommitId().substring(0, 8));
        diffOutput.add("  Message: " + commit1.getMessage());
        diffOutput.add("  Date: " + commit1.getTimestamp());
        diffOutput.add("");
        diffOutput.add("TO COMMIT: " + commit2.getCommitId().substring(0, 8));
        diffOutput.add("  Message: " + commit2.getMessage());
        diffOutput.add("  Date: " + commit2.getTimestamp());
        diffOutput.add("");

        // Summary statistics
        diffOutput.add("┌────────────────────────────────────────────────────────────────────────┐");
        diffOutput.add("│ SUMMARY                                                                │");
        diffOutput.add("├────────────────────────────────────────────────────────────────────────┤");
        diffOutput.add(String.format("│ Files Added:     %3d                                                 │", addedFiles.size()));
        diffOutput.add(String.format("│ Files Removed:   %3d                                                 │", removedFiles.size()));
        diffOutput.add(String.format("│ Files Modified:  %3d                                                 │", modifiedFiles.size()));
        diffOutput.add(String.format("│ Files Unchanged: %3d                                                 │", files1.size() - removedFiles.size() - modifiedFiles.size()));
        diffOutput.add("└────────────────────────────────────────────────────────────────────────┘");
        diffOutput.add("");

        // Added files
        if (!addedFiles.isEmpty()) {
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            diffOutput.add("✨ NEW FILES (" + addedFiles.size() + ")");
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            addedFiles.stream().sorted().forEach(file -> {
                diffOutput.add("  ➕ " + file);
                // Show file contents
                String filePath = repoPath + File.separator + "data" + File.separator + "commits" +
                                 File.separator + "commit_" + commit2.getCommitId() + File.separator +
                                 new File(file).getName();
                List<String> contents = readFileContents(filePath);
                if (!contents.isEmpty()) {
                    diffOutput.add("     Lines: " + contents.size());
                    for (int i = 0; i < Math.min(3, contents.size()); i++) {
                        diffOutput.add("     >>> " + contents.get(i));
                    }
                    if (contents.size() > 3) {
                        diffOutput.add("     ... and " + (contents.size() - 3) + " more lines");
                    }
                }
            });
            diffOutput.add("");
        }

        // Removed files
        if (!removedFiles.isEmpty()) {
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            diffOutput.add("❌ DELETED FILES (" + removedFiles.size() + ")");
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            removedFiles.stream().sorted().forEach(file -> {
                diffOutput.add("  ➖ " + file);
                // Show old file contents
                String filePath = repoPath + File.separator + "data" + File.separator + "commits" +
                                 File.separator + "commit_" + commit1.getCommitId() + File.separator +
                                 new File(file).getName();
                List<String> contents = readFileContents(filePath);
                if (!contents.isEmpty()) {
                    diffOutput.add("     Lines: " + contents.size());
                    for (int i = 0; i < Math.min(3, contents.size()); i++) {
                        diffOutput.add("     <<< " + contents.get(i));
                    }
                    if (contents.size() > 3) {
                        diffOutput.add("     ... and " + (contents.size() - 3) + " more lines");
                    }
                }
            });
            diffOutput.add("");
        }

        // Modified files - DETAILED LINE BY LINE DIFF
        if (!modifiedFiles.isEmpty()) {
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            diffOutput.add("🔄 MODIFIED FILES (" + modifiedFiles.size() + ")");
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");

            for (String file : modifiedFiles.stream().sorted().collect(java.util.stream.Collectors.toList())) {
                diffOutput.add("");
                diffOutput.add("  📝 " + file);
                diffOutput.add("  " + "─".repeat(70));

                // Get file contents from both commits
                String oldPath = repoPath + File.separator + "data" + File.separator + "commits" +
                                File.separator + "commit_" + commit1.getCommitId() + File.separator +
                                new File(file).getName();
                String newPath = repoPath + File.separator + "data" + File.separator + "commits" +
                                File.separator + "commit_" + commit2.getCommitId() + File.separator +
                                new File(file).getName();

                List<String> oldLines = readFileContents(oldPath);
                List<String> newLines = readFileContents(newPath);

                // Generate line-by-line diff
                List<String> diffLines = generateLineDiff(oldLines, newLines);

                if (diffLines.isEmpty()) {
                    diffOutput.add("  (No differences found)");
                } else {
                    for (String diffLine : diffLines) {
                        diffOutput.add(diffLine);
                    }
                }

                // Statistics for this file
                int additions = (int) diffLines.stream().filter(l -> l.contains("  ➕")).count();
                int deletions = (int) diffLines.stream().filter(l -> l.contains("  ➖")).count();
                diffOutput.add("");
                diffOutput.add(String.format("  Changes: +%d -%d | Old: %d lines, New: %d lines",
                    additions, deletions, oldLines.size(), newLines.size()));
            }
            diffOutput.add("");
        }

        // Unchanged files summary
        int unchangedCount = files1.size() - removedFiles.size() - modifiedFiles.size();
        if (unchangedCount > 0) {
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            diffOutput.add("✅ UNCHANGED FILES (" + unchangedCount + ")");
            diffOutput.add("───────────────────────────────────────────────────────────────────────────");
            files1.stream()
                .filter(f -> !removedFiles.contains(f) && !modifiedFiles.contains(f))
                .sorted()
                .forEach(file -> diffOutput.add("  ➜ " + file));
            diffOutput.add("");
        }

        // Final summary
        diffOutput.add("╔════════════════════════════════════════════════════════════════════════╗");
        int totalAdded = addedFiles.size();
        int totalRemoved = removedFiles.size();
        int totalModified = modifiedFiles.size();
        diffOutput.add(String.format("║ Total: %d file(s) added, %d removed, %d modified, %d unchanged        ║",
            totalAdded, totalRemoved, totalModified, unchangedCount));
        diffOutput.add("╚════════════════════════════════════════════════════════════════════════╝");

        return diffOutput;
    }

    /**
     * Generate line-by-line diff using simple algorithm
     * Shows added, removed, and context lines
     */
    private List<String> generateLineDiff(List<String> oldLines, List<String> newLines) {
        List<String> diffLines = new ArrayList<>();

        if (oldLines.isEmpty() && newLines.isEmpty()) {
            return diffLines;
        }

        if (oldLines.isEmpty()) {
            // All lines added
            for (int i = 0; i < newLines.size(); i++) {
                diffLines.add(String.format("  ➕ [%3d] %s", i + 1, newLines.get(i)));
            }
            return diffLines;
        }

        if (newLines.isEmpty()) {
            // All lines removed
            for (int i = 0; i < oldLines.size(); i++) {
                diffLines.add(String.format("  ➖ [%3d] %s", i + 1, oldLines.get(i)));
            }
            return diffLines;
        }

        // Use simple diff algorithm (line-by-line comparison)
        Set<String> oldSet = new HashSet<>(oldLines);
        Set<String> newSet = new HashSet<>(newLines);

        // Show all lines with indicators
        for (int i = 0; i < oldLines.size(); i++) {
            String line = oldLines.get(i);
            if (!newSet.contains(line)) {
                diffLines.add(String.format("  ➖ [%3d] %s", i + 1, line));
            }
        }

        for (int i = 0; i < newLines.size(); i++) {
            String line = newLines.get(i);
            if (!oldSet.contains(line)) {
                diffLines.add(String.format("  ➕ [%3d] %s", i + 1, line));
            }
        }

        return diffLines;
    }

    /**
     * Read file contents into list of lines
     */
    private List<String> readFileContents(String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return lines;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
        }
        return lines;
    }

    /**
     * Legacy method - Compare commits with file-level summary
     */
    public List<String> compareCommits(Commit commit1, Commit commit2) {
        List<String> diffLines = new ArrayList<>();

        if (commit1 == null || commit2 == null) {
            diffLines.add("Cannot compare: one or both commits are null");
            return diffLines;
        }

        Set<String> files1 = new HashSet<>(commit1.getChangedFiles());
        Set<String> files2 = new HashSet<>(commit2.getChangedFiles());

        Set<String> allFiles = new HashSet<>(files1);
        allFiles.addAll(files2);

        if (allFiles.isEmpty()) {
            diffLines.add("No files in either commit");
            return diffLines;
        }

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

        diffLines.add("=== File Changes Summary ===");
        diffLines.add("Added files: " + addedFiles.size());
        diffLines.add("Removed files: " + removedFiles.size());
        diffLines.add("Modified files: " + commonFiles.size());
        diffLines.add("");

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
            diffLines.add("Modified Files:");
            Collections.sort(commonFiles);
            for (String file : commonFiles) {
                diffLines.add("  ~ " + file);
            }
        }

        return diffLines;
    }

    /**
     * Get added lines between two files
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
            Set<String> oldLines = new HashSet<>(readLines(oldFile));
            List<String> newLines = readLines(newFile);

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
     * Get removed lines between two files
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
            List<String> oldLines = readLines(oldFile);
            Set<String> newLines = new HashSet<>(readLines(newFile));

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
     * Read lines from a file
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
}

