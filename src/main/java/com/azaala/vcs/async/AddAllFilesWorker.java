package com.azaala.vcs.async;

import com.azaala.vcs.VCS;
import com.azaala.vcs.Repository;
import com.azaala.vcs.FileHandler;
import com.azaala.vcs.Utils;
import com.azaala.vcs.persistence.dao.StagedFileDAO;
import com.azaala.vcs.persistence.dao.ActivityLogDAO;
import com.azaala.vcs.persistence.dao.RepositoryDAO;
import com.azaala.vcs.persistence.models.StagedFileEntity;
import com.azaala.vcs.persistence.models.ActivityLogEntity;
import com.azaala.vcs.persistence.models.RepositoryEntity;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AddAllFilesWorker - Recursively stages all files from a directory and subdirectories
 * Handles bulk file addition with directory structure preservation
 */
public class AddAllFilesWorker extends BaseVCSWorker<Integer> {
    private final Repository repository;
    private final String directoryPath;
    private final List<String> excludePatterns;
    private final StagedFileDAO stagedFileDAO;
    private final ActivityLogDAO activityLogDAO;
    private final FileHandler fileHandler;
    private int totalFilesProcessed = 0;
    private int filesSuccessfullyAdded = 0;
    private int filesSkipped = 0;
    private final List<String> addedFiles = new ArrayList<>();
    private final List<String> skippedFiles = new ArrayList<>();

    /**
     * Constructor for AddAllFilesWorker
     *
     * @param repository Repository instance
     * @param directoryPath Path to directory to add files from
     * @param progressListener Progress callback listener
     */
    public AddAllFilesWorker(VCS vcs, Repository repository, String directoryPath, ProgressListener progressListener) {
        super(progressListener);
        this.repository = repository;
        this.directoryPath = directoryPath;
        this.stagedFileDAO = new StagedFileDAO();
        this.activityLogDAO = new ActivityLogDAO();
        this.fileHandler = new FileHandler();

        // Default exclude patterns (repository metadata folders)
        this.excludePatterns = new ArrayList<>(Arrays.asList(
            ".azaala",
            ".git",
            ".svn",
            "data",
            "target",
            "build",
            ".idea",
            ".vscode"
        ));
    }

    /**
     * Add custom exclude patterns
     *
     * @param patterns Patterns to exclude
     */
    public void addExcludePatterns(List<String> patterns) {
        if (patterns != null) {
            this.excludePatterns.addAll(patterns);
        }
    }

    /**
     * Background task: Recursively collect and stage all files
     */
    @Override
    protected Integer doInBackground() throws Exception {
        Long repoId = null;
        try {
            File directory = new File(directoryPath);
            if (!directory.exists() || !directory.isDirectory()) {
                throw new Exception("Directory does not exist: " + directoryPath);
            }

            // Resolve repository ID from database if not already set
            publishProgress("Resolving repository information...", 2);
            if (repository.getRepoId() == null) {
                try {
                    RepositoryDAO repoDAO = new RepositoryDAO();
                    RepositoryEntity repoEntity = repoDAO.findByPath(repository.getPath());
                    if (repoEntity != null) {
                        repoId = repoEntity.getRepoId();
                        repository.setRepoId(repoId);
                    } else {
                        System.out.println("Note: Repository not found in database. Database logging will be skipped.");
                        repoId = null;
                    }
                } catch (Exception e) {
                    System.out.println("Note: Could not resolve repository ID from database: " + e.getMessage());
                    repoId = null;
                }
            } else {
                repoId = repository.getRepoId();
            }

            // Step 1: Collect all files recursively
            publishProgress("Scanning directory structure...", 5);
            List<String> allFiles = fileHandler.getAllFilesRecursive(directoryPath, excludePatterns);
            totalFilesProcessed = allFiles.size();

            if (totalFilesProcessed == 0) {
                publishProgress("No files found to add", 50);
                return 0;
            }

            // Get already staged files to avoid processing duplicates
            List<String> currentStagedFiles = new java.util.ArrayList<>(repository.getStagedFiles());
            System.out.println("[INFO] Repository has " + currentStagedFiles.size() + " already staged files:");
            for (String f : currentStagedFiles) {
                System.out.println("[INFO]   - " + f);
            }

            // Normalize staged file paths for comparison
            java.util.Set<String> normalizedStagedPaths = new java.util.HashSet<>();
            for (String stagedPath : currentStagedFiles) {
                try {
                    String normalized = new java.io.File(stagedPath).getCanonicalPath().toLowerCase();
                    normalizedStagedPaths.add(normalized);
                    System.out.println("[DEBUG] Normalized staged path: " + normalized);
                } catch (Exception e) {
                    String normalized = new java.io.File(stagedPath).getAbsolutePath().toLowerCase();
                    normalizedStagedPaths.add(normalized);
                    System.out.println("[DEBUG] Fallback normalized staged path: " + normalized);
                }
            }

            // Filter out already staged files from processing
            List<String> filesToProcess = new java.util.ArrayList<>();
            System.out.println("[INFO] Scanning " + allFiles.size() + " files to find new ones...");

            for (String file : allFiles) {
                try {
                    String normalizedFile = new java.io.File(file).getCanonicalPath().toLowerCase();
                    System.out.println("[DEBUG] Checking file: " + normalizedFile);

                    if (!normalizedStagedPaths.contains(normalizedFile)) {
                        filesToProcess.add(file);
                        System.out.println("[DEBUG]   → NEW FILE (not in staged)");
                    } else {
                        filesSkipped++;
                        skippedFiles.add(file);
                        System.out.println("[DEBUG]   → SKIPPED (already staged)");
                    }
                } catch (Exception e) {
                    // Fallback to absolute path comparison
                    String absPath = new java.io.File(file).getAbsolutePath().toLowerCase();
                    System.out.println("[DEBUG] Using fallback path: " + absPath);

                    if (!normalizedStagedPaths.contains(absPath)) {
                        filesToProcess.add(file);
                        System.out.println("[DEBUG]   → NEW FILE (not in staged)");
                    } else {
                        filesSkipped++;
                        skippedFiles.add(file);
                        System.out.println("[DEBUG]   → SKIPPED (already staged)");
                    }
                }
            }

            System.out.println("Found " + totalFilesProcessed + " total files");
            System.out.println("  - Already staged: " + filesSkipped);
            System.out.println("  - Ready to add: " + filesToProcess.size());

            if (filesToProcess.isEmpty()) {
                publishProgress("All files already staged", 50);
                return 0;
            }

            publishProgress("Found " + filesToProcess.size() + " new files to add", 15);

            // Step 2: Process each file that needs to be added
            File repoRoot = new File(repository.getPath());
            String indexPath = repository.getPath() + File.separator + "data" + File.separator + "index";

            for (int i = 0; i < filesToProcess.size(); i++) {
                String filePath = filesToProcess.get(i);

                // Check if cancelled
                if (isCancelled()) {
                    publishProgress("Operation cancelled by user", 100);
                    break;
                }

                // Calculate progress
                int progress = 15 + (int) ((i / (double) filesToProcess.size()) * 70);
                String fileName = new File(filePath).getName();
                publishProgress("Adding: " + fileName + " (" + (i + 1) + "/" + filesToProcess.size() + ")", progress);

                // Verify file is within repository boundaries
                if (!Utils.isFileWithinDirectory(new File(filePath), repoRoot.getParentFile())) {
                    filesSkipped++;
                    skippedFiles.add(filePath);
                    System.out.println("  ✗ Outside repository boundary: " + filePath);
                    continue;
                }

                try {
                    // Copy file to index with directory structure preserved
                    if (fileHandler.copyToIndexWithStructure(filePath, indexPath, repository.getPath())) {
                        // Stage the file
                        if (repository.stageFile(filePath)) {
                            filesSuccessfullyAdded++;
                            addedFiles.add(filePath);
                            System.out.println("  ✓ Added: " + filePath);

                            // Log to database only if repository ID is available
                            if (repoId != null) {
                                try {
                                    long fileSize = fileHandler.getFileSize(filePath);
                                    LocalDateTime now = LocalDateTime.now();

                                    StagedFileEntity stagedFile = new StagedFileEntity();
                                    stagedFile.setRepoId(repoId);
                                    stagedFile.setFilePath(filePath);
                                    stagedFile.setFileSize(fileSize);
                                    stagedFile.setLastModified(now);
                                    stagedFile.setStatus("staged");
                                    stagedFile.setCreatedAt(now);
                                    stagedFileDAO.create(stagedFile);
                                } catch (Exception e) {
                                    System.err.println("Warning: Failed to log staged file to database: " + e.getMessage());
                                }
                            }
                        } else {
                            filesSkipped++;
                            skippedFiles.add(filePath);
                            System.out.println("  ✗ Failed to stage: " + filePath);
                        }
                    } else {
                        filesSkipped++;
                        skippedFiles.add(filePath);
                        System.out.println("  ✗ Failed to copy to index: " + filePath);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing file " + filePath + ": " + e.getMessage());
                    filesSkipped++;
                    skippedFiles.add(filePath);
                }
            }

            // Step 3: Log activity to database if repository ID is available
            publishProgress("Finalizing...", 90);
            if (repoId != null) {
                try {
                    ActivityLogEntity log = new ActivityLogEntity();
                    log.setRepoId(repoId);
                    log.setOperation("BULK_ADD_FILES");
                    log.setDetails("Added " + filesSuccessfullyAdded + " files from directory: " + directoryPath +
                                 " | Total: " + totalFilesProcessed + ", Added: " + filesSuccessfullyAdded +
                                 ", Skipped: " + filesSkipped);
                    log.setTimestamp(LocalDateTime.now());
                    log.setCreatedAt(LocalDateTime.now());
                    activityLogDAO.create(log);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to log activity: " + e.getMessage());
                }
            }

            publishProgress("Complete", 100);
            return filesSuccessfullyAdded;

        } catch (Exception e) {
            System.err.println("Error in AddAllFilesWorker: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get count of successfully added files
     */
    public int getFilesAddedCount() {
        return filesSuccessfullyAdded;
    }

    /**
     * Get count of skipped files
     */
    public int getFilesSkippedCount() {
        return filesSkipped;
    }

    /**
     * Get list of added files
     */
    public List<String> getAddedFiles() {
        return new ArrayList<>(addedFiles);
    }

    /**
     * Get list of skipped files
     */
    public List<String> getSkippedFiles() {
        return new ArrayList<>(skippedFiles);
    }

    /**
     * Get total files processed
     */
    public int getTotalFilesProcessed() {
        return totalFilesProcessed;
    }
}
