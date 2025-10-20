package com.azaala.vcs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class providing common helper methods for the VCS system.
 */
public class Utils {
    // Improved pattern to handle more valid path characters
    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9._/\\\\:\\-\\s]+$");

    /**
     * Generates a unique ID for commits.
     *
     * @return Unique 8-character ID
     */
    public static String generateUniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Validates if a path is safe and valid.
     *
     * @param path Path to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        String trimmedPath = path.trim();

        // Check for dangerous patterns
        if (trimmedPath.contains("..") || trimmedPath.contains("//") ||
                trimmedPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("windows")) {
            return false;
        }

        return VALID_PATH_PATTERN.matcher(trimmedPath).matches();
    }

    /**
     * Checks if a file is within a specified directory.
     *
     * @param file File to check
     * @param directory Directory boundary
     * @return true if file is within directory, false otherwise
     */
    public static boolean isFileWithinDirectory(File file, File directory) {
        try {
            Path filePath = file.getCanonicalFile().toPath();
            Path dirPath = directory.getCanonicalFile().toPath();
            return filePath.startsWith(dirPath);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Gets the filename from a path.
     *
     * @param filePath Full file path
     * @return Just the filename
     */
    public static String getFileName(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return "";
        }

        File file = new File(filePath.trim());
        return file.getName();
    }

    /**
     * Gets relative path from file to directory.
     *
     * @param file File to get relative path for
     * @param baseDir Base directory
     * @return Relative path string
     */
    public static String getRelativePath(File file, File baseDir) {
        try {
            Path filePath = file.getCanonicalFile().toPath();
            Path basePath = baseDir.getCanonicalFile().toPath();
            return basePath.relativize(filePath).toString();
        } catch (IOException e) {
            return file.getName();
        }
    }

    /**
     * Safely trims a string.
     *
     * @param str String to trim
     * @return Trimmed string or empty string if null
     */
    public static String safeTrim(String str) {
        return str != null ? str.trim() : "";
    }

    /**
     * Checks if a string is null or empty after trimming.
     *
     * @param str String to check
     * @return true if null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Generates a hash of the file content using SHA-256.
     *
     * @param file File to hash
     * @return Hex string of the hash, or null if error
     */
    public static String hashFileContent(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
            byte[] hashBytes = digest.digest(fileBytes);

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Error hashing file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates a human-readable timestamp string.
     *
     * @return Formatted current timestamp
     */
    public static String generateTimestamp() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
