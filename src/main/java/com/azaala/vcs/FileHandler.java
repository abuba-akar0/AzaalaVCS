package com.azaala.vcs;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles file operations for the VCS system.
 * Manages reading, writing, copying files and commit metadata.
 */
public class FileHandler {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Reads the content of a file.
     *
     * @param filePath Path to the file to read
     * @return File content as string, or null if error occurred
     */
    public String readFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }

        try {
            return Files.readString(Paths.get(filePath.trim()));
        } catch (IOException e) {
            System.err.println("Error reading file '" + filePath + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Writes content to a file.
     *
     * @param filePath Path where to write the file
     * @param content Content to write
     * @return true if successful, false otherwise
     */
    public boolean writeFile(String filePath, String content) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath.trim());

            // Create parent directories if they don't exist
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            Files.writeString(path, content != null ? content : "");
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file '" + filePath + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Copies a file to the index directory.
     *
     * @param sourceFilePath Source file path
     * @param indexPath Index directory path
     * @return true if successful, false otherwise
     */
    public boolean copyToIndex(String sourceFilePath, String indexPath) {
        if (sourceFilePath == null || indexPath == null) {
            return false;
        }

        try {
            Path sourcePath = Paths.get(sourceFilePath.trim());
            Path indexDir = Paths.get(indexPath.trim());

            // Create index directory if it doesn't exist
            Files.createDirectories(indexDir);

            // Get just the filename for the target
            String fileName = sourcePath.getFileName().toString();
            Path targetPath = indexDir.resolve(fileName);

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error copying file to index: " + e.getMessage());
            return false;
        }
    }

    /**
     * Copies a file from source to target location.
     *
     * @param sourcePath Source file path
     * @param targetPath Target file path
     * @return true if successful, false otherwise
     */
    public boolean copyFile(String sourcePath, String targetPath) {
        if (sourcePath == null || targetPath == null) {
            return false;
        }

        try {
            Path source = Paths.get(sourcePath.trim());
            Path target = Paths.get(targetPath.trim());

            // Create parent directories if they don't exist
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error copying file from '" + sourcePath + "' to '" + targetPath + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Copies a file from source to destination.
     *
     * @param source Source file
     * @param dest Destination file
     * @return true if successful, false otherwise
     */
    public boolean copyFile(File source, File dest) {
        if (source == null || dest == null) {
            return false;
        }

        try {
            return copyFile(source.getPath(), dest.getPath());
        } catch (Exception e) {
            System.err.println("Error copying file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves commit metadata to a file.
     *
     * @param commit The commit to save
     * @param commitPath Directory path for the commit
     * @return true if successful, false otherwise
     */
    public boolean saveCommit(Commit commit, String commitPath) {
        if (commit == null || commitPath == null) {
            return false;
        }

        try {
            Path commitDir = Paths.get(commitPath.trim());
            Files.createDirectories(commitDir);

            Path metadataFile = commitDir.resolve("metadata.txt");

            StringBuilder metadata = new StringBuilder();
            metadata.append("COMMIT_ID=").append(commit.getCommitId()).append("\n");
            metadata.append("MESSAGE=").append(commit.getMessage()).append("\n");
            metadata.append("TIMESTAMP=").append(commit.getTimestamp().format(TIMESTAMP_FORMATTER)).append("\n");
            metadata.append("SUMMARY=").append(commit.getSummary()).append("\n");
            metadata.append("FILE_COUNT=").append(commit.getFileCount()).append("\n");
            metadata.append("FILES=").append(String.join(",", commit.getChangedFiles())).append("\n");

            return writeFile(metadataFile.toString(), metadata.toString());
        } catch (Exception e) {
            System.err.println("Error saving commit metadata: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reads all lines from a file.
     *
     * @param filePath Path to the file
     * @return List of lines, or empty list if error occurred
     */
    public List<String> readLines(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return Files.readAllLines(Paths.get(filePath.trim()));
        } catch (IOException e) {
            System.err.println("Error reading lines from '" + filePath + "': " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Writes lines to a file.
     *
     * @param filePath Path where to write
     * @param lines Lines to write
     * @return true if successful, false otherwise
     */
    public boolean writeLines(String filePath, List<String> lines) {
        if (filePath == null || lines == null) {
            return false;
        }

        try {
            Path path = Paths.get(filePath.trim());

            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            Files.write(path, lines);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing lines to '" + filePath + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a file exists.
     *
     * @param filePath Path to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        return Files.exists(Paths.get(filePath.trim()));
    }

    /**
     * Deletes a file.
     *
     * @param filePath Path to the file to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        try {
            return Files.deleteIfExists(Paths.get(filePath.trim()));
        } catch (IOException e) {
            System.err.println("Error deleting file '" + filePath + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates directories recursively.
     *
     * @param directoryPath Path to create
     * @return true if successful, false otherwise
     */
    public boolean createDirectories(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return false;
        }

        try {
            Files.createDirectories(Paths.get(directoryPath.trim()));
            return true;
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the size of a file in bytes.
     *
     * @param filePath Path to the file
     * @return File size in bytes, or -1 if error
     */
    public long getFileSize(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return -1;
        }

        try {
            return Files.size(Paths.get(filePath.trim()));
        } catch (IOException e) {
            System.err.println("Error getting file size: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Recursively collects all files from a directory and its subdirectories.
     * Preserves the directory structure in relative paths.
     *
     * @param directoryPath Root directory path
     * @param excludePatterns List of patterns to exclude (e.g., ".azaala", ".git")
     * @return List of absolute file paths found recursively
     */
    public List<String> getAllFilesRecursive(String directoryPath, List<String> excludePatterns) {
        List<String> allFiles = new ArrayList<>();
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return allFiles;
        }

        try {
            Path rootPath = Paths.get(directoryPath.trim());
            Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .filter(path -> !isExcluded(path, rootPath, excludePatterns))
                .forEach(path -> allFiles.add(path.toAbsolutePath().toString()));

            return allFiles;
        } catch (IOException e) {
            System.err.println("Error collecting files recursively: " + e.getMessage());
            return allFiles;
        }
    }

    /**
     * Checks if a path should be excluded based on patterns.
     *
     * @param path File path to check
     * @param rootPath Root directory path for relative comparison
     * @param excludePatterns Patterns to exclude
     * @return true if path should be excluded, false otherwise
     */
    private boolean isExcluded(Path path, Path rootPath, List<String> excludePatterns) {
        if (excludePatterns == null || excludePatterns.isEmpty()) {
            return false;
        }

        Path relativePath = rootPath.relativize(path);
        String relativePathStr = relativePath.toString();

        for (String pattern : excludePatterns) {
            if (relativePathStr.contains(File.separator + pattern + File.separator) ||
                relativePathStr.startsWith(pattern + File.separator) ||
                relativePathStr.endsWith(File.separator + pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies a file to index while preserving directory structure.
     * Creates subdirectories as needed to maintain folder hierarchy.
     *
     * @param sourceFilePath Absolute source file path
     * @param indexPath Index directory path
     * @param repositoryPath Repository root path (for relative path calculation)
     * @return true if successful, false otherwise
     */
    public boolean copyToIndexWithStructure(String sourceFilePath, String indexPath, String repositoryPath) {
        if (sourceFilePath == null || indexPath == null || repositoryPath == null) {
            return false;
        }

        try {
            Path sourcePath = Paths.get(sourceFilePath.trim());
            Path repoPath = Paths.get(repositoryPath.trim());
            Path indexDir = Paths.get(indexPath.trim());

            // Create index directory if it doesn't exist
            Files.createDirectories(indexDir);

            // Calculate relative path from repository root
            Path relativePath = repoPath.relativize(sourcePath);

            // Create target path preserving directory structure
            Path targetPath = indexDir.resolve(relativePath);

            // Create parent directories in index
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // Copy the file
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Error copying file to index with structure: " + e.getMessage());
            return false;
        }
    }

    /**
     * Batch copies multiple files to index while preserving directory structure.
     * More efficient than copying files one by one.
     *
     * @param sourceFiles List of absolute file paths to copy
     * @param indexPath Index directory path
     * @param repositoryPath Repository root path
     * @return Number of files successfully copied
     */
    public int copyFilesWithStructure(List<String> sourceFiles, String indexPath, String repositoryPath) {
        int successCount = 0;
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return 0;
        }

        for (String sourceFile : sourceFiles) {
            if (copyToIndexWithStructure(sourceFile, indexPath, repositoryPath)) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * Gets relative path from repository root to a file.
     * Useful for storing standardized paths.
     *
     * @param filePath Absolute file path
     * @param repositoryPath Repository root path
     * @return Relative path string, or original path if calculation fails
     */
    public String getRelativePath(String filePath, String repositoryPath) {
        if (filePath == null || repositoryPath == null) {
            return filePath;
        }

        try {
            Path file = Paths.get(filePath.trim());
            Path repo = Paths.get(repositoryPath.trim());
            Path relativePath = repo.relativize(file);
            return relativePath.toString();
        } catch (Exception e) {
            System.err.println("Error calculating relative path: " + e.getMessage());
            return filePath;
        }
    }

    /**
     * Gets directory structure info for a given directory.
     * Returns count of files and subdirectories.
     *
     * @param directoryPath Directory to analyze
     * @return String with directory statistics
     */
    public String getDirectoryStructureInfo(String directoryPath) {
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return "Invalid directory path";
        }

        try {
            Path rootPath = Paths.get(directoryPath.trim());
            long fileCount = Files.walk(rootPath)
                .filter(Files::isRegularFile)
                .count();

            long dirCount = Files.walk(rootPath)
                .filter(Files::isDirectory)
                .count() - 1; // Subtract 1 for the root directory itself

            return String.format("Files: %d, Subdirectories: %d", fileCount, dirCount);
        } catch (IOException e) {
            System.err.println("Error getting directory structure info: " + e.getMessage());
            return "Error analyzing directory";
        }
    }
}
