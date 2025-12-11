package com.azaala.vcs.async;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Abstract base class for VCS SwingWorker operations.
 * Provides common functionality for asynchronous background tasks with UI updates.
 *
 * @param <T> The return type of the background computation
 */
public abstract class BaseVCSWorker<T> extends SwingWorker<T, String> {
    private ProgressListener progressListener;

    public BaseVCSWorker(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public BaseVCSWorker() {
        this(null);
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                T result = get();
                onSuccess(result);
                notifySuccess(result);
            } else {
                onCancellation();
                notifyProgress("Operation cancelled", 0);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            onError(new Exception("Operation interrupted", e));
            notifyError("Operation was interrupted", e);
        } catch (ExecutionException e) {
            onError(e.getCause() != null ? (Exception) e.getCause() : new Exception(e));
            notifyError("Error during execution: " + e.getMessage(), e.getCause());
        } catch (Exception e) {
            onError(e);
            notifyError("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void process(List<String> chunks) {
        for (String chunk : chunks) {
            notifyProgress(chunk, getProgress());
        }
    }

    protected void publishProgress(String message, int progress) {
        publish(message);
        notifyProgress(message, progress);
    }

    protected void publishProgress(String message) {
        publish(message);
    }

    protected void notifyProgress(String message, int progress) {
        if (progressListener != null) {
            try {
                progressListener.onProgress(message, progress);
            } catch (Exception e) {
                System.err.println("Error in progress listener: " + e.getMessage());
            }
        }
    }

    protected void notifySuccess(T result) {
        if (progressListener != null) {
            try {
                progressListener.onSuccess(result != null ? result.toString() : "Completed successfully");
            } catch (Exception e) {
                System.err.println("Error in progress listener: " + e.getMessage());
            }
        }
    }

    protected void notifyError(String message, Throwable exception) {
        if (progressListener != null) {
            try {
                progressListener.onError(message, exception);
            } catch (Exception e) {
                System.err.println("Error in progress listener: " + e.getMessage());
            }
        }
    }

    protected void onSuccess(T result) {
        // Override in subclasses for custom success handling
    }

    protected void onCancellation() {
        // Override in subclasses for custom cancellation handling
    }

    protected void onError(Exception exception) {
        exception.printStackTrace();
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    @Override
    protected abstract T doInBackground() throws Exception;
}

