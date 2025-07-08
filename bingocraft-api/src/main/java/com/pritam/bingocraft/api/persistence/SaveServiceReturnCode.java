package com.pritam.bingocraft.api.persistence;

/**
 * Enumeration of possible return codes from {@link SaveService} operations.
 *
 * <p>These codes provide detailed information about the result of persistence
 * operations, allowing callers to handle different scenarios appropriately.
 *
 * @since 1.0.0
 * @author Pritam
 * @see SaveService
 */
public enum SaveServiceReturnCode {

    /**
     * The SaveService is offline or disabled.
     *
     * <p>This typically occurs when the database connection failed during
     * initialization or the service was explicitly disabled in configuration.
     */
    OFFLINE,

    /**
     * The operation completed successfully.
     *
     * <p>For save operations, this means the object was queued for persistence.
     * For load operations, this means the object was found and loaded.
     * For delete operations, this means the object was removed.
     */
    SUCCESS,

    /**
     * A SQL database error occurred during the operation.
     *
     * <p>This indicates an unexpected database-level error such as
     * connection loss, constraint violations, or SQL syntax errors.
     * Check the server logs for detailed error information.
     */
    SQL_ERROR,

    /**
     * The specified key was not found in the storage.
     *
     * <p>This is returned by load operations when no object exists
     * with the given key, or by delete operations when attempting
     * to delete a non-existent key.
     */
    KEY_NOT_FOUND,

    /**
     * An object exists with the specified key.
     *
     * <p>This is returned by exists() operations when the key
     * is found in either the cache or persistent storage.
     */
    EXISTS,

    /**
     * No object exists with the specified key.
     *
     * <p>This is returned by exists() operations when the key
     * is not found in either the cache or persistent storage.
     */
    NOT_EXISTS,

    /**
     * The provided key is invalid.
     *
     * <p>This occurs when the key parameter is null, empty,
     * or contains only whitespace characters.
     */
    INVALID_KEY,
}
