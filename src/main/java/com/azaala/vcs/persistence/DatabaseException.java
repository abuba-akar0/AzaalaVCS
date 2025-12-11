package com.azaala.vcs.persistence;

/**
 * Custom exception class for database-related errors.
 * Used throughout the persistence layer for error handling and reporting.
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}


