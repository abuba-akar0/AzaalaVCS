package com.azaala.vcs.gui;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages saved repository paths for quick access
 */
public class RepositoryManager {

    private static final String REPOS_FILE = System.getProperty("user.home") + File.separator + ".azaala_repos";
    private List<String> repositoryPaths;

    public RepositoryManager() {
        repositoryPaths = new ArrayList<>();
        loadRepositories();
    }

    /**
     * Add a repository path and save
     */
    public void addRepository(String path) {
        if (!repositoryPaths.contains(path)) {
            repositoryPaths.add(path);
            saveRepositories();
        }
    }

    /**
     * Remove a repository path and save
     */
    public void removeRepository(String path) {
        repositoryPaths.remove(path);
        saveRepositories();
    }

    /**
     * Get all saved repository paths
     */
    public List<String> getRepositories() {
        return new ArrayList<>(repositoryPaths);
    }

    /**
     * Load repositories from file
     */
    private void loadRepositories() {
        try {
            File file = new File(REPOS_FILE);
            if (file.exists()) {
                List<String> lines = Files.readAllLines(file.toPath());
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        File repoDir = new File(trimmed);
                        if (repoDir.exists() && repoDir.isDirectory()) {
                            repositoryPaths.add(trimmed);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading repositories: " + e.getMessage());
        }
    }

    /**
     * Save repositories to file
     */
    private void saveRepositories() {
        try {
            File file = new File(REPOS_FILE);
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), repositoryPaths);
        } catch (IOException e) {
            System.err.println("Error saving repositories: " + e.getMessage());
        }
    }
}

