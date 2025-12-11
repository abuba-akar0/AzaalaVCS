package com.azaala.vcs.async;

/**
 * Listener interface for receiving progress updates from SwingWorker operations.
 * Implementations should be thread-safe as callbacks may occur from different threads.
 */
public interface ProgressListener {
    /**
     * Called when progress has been made on the background task.
     * This is called on the EDT (Event Dispatch Thread).
     *
     * @param message The progress message
     * @param progress The progress percentage (0-100)
     */
    void onProgress(String message, int progress);

    /**
     * Called when the background task completes successfully.
     * This is called on the EDT (Event Dispatch Thread).
     *
     * @param result The result message or description
     */
    void onSuccess(String result);

    /**
     * Called when an error occurs during background execution.
     * This is called on the EDT (Event Dispatch Thread).
     *
     * @param message The error message
     * @param exception The exception that occurred (may be null)
     */
    void onError(String message, Throwable exception);
}