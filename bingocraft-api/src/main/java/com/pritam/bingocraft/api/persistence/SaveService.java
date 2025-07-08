package com.pritam.bingocraft.api.persistence;

import java.util.concurrent.CompletableFuture;

/**
 * A service for persisting and retrieving objects that implement {@link SaveableObject}.
 *
 * <p>This service provides asynchronous operations for saving, loading, deleting, and checking
 * the existence of objects in a persistent storage layer (typically SQLite database).
 *
 * <p>All operations use string keys to identify objects and return {@link SaveServiceReturnCode}
 * to indicate the result of the operation.
 *
 * <p>The service is designed to be thread-safe and uses caching for improved performance.
 *
 * @since 1.0.0
 * @author Pritam
 */
public interface SaveService {

    /**
     * Saves an object to persistent storage using the specified key.
     *
     * <p>This operation is synchronous and typically queues the object for batch writing
     * to improve performance. The object is immediately available in cache for subsequent
     * load operations.
     *
     * @param key the unique identifier for the object, must not be null or blank
     * @param object the object to save, must implement {@link SaveableObject}
     * @return {@link SaveServiceReturnCode#SUCCESS} if the object was queued for saving,
     *         {@link SaveServiceReturnCode#OFFLINE} if the service is disabled,
     *         {@link SaveServiceReturnCode#INVALID_KEY} if the key is null or blank
     * @throws NullPointerException if object is null
     */
    SaveServiceReturnCode save(String key, SaveableObject object);

    /**
     * Loads an object from persistent storage or cache using the specified key.
     *
     * <p>This operation is asynchronous and will first check the cache before
     * querying the database. The loaded data is deserialized into the provided
     * empty instance using {@link SaveableObject#fromString(String)}.
     *
     * @param <T> the type of object to load, must extend {@link SaveableObject}
     * @param key the unique identifier for the object, must not be null or blank
     * @param emptyInstance an empty instance of the object type to populate with loaded data
     * @return a {@link CompletableFuture} that completes with:
     *         <ul>
     *         <li>{@link SaveServiceReturnCode#SUCCESS} if the object was loaded successfully</li>
     *         <li>{@link SaveServiceReturnCode#KEY_NOT_FOUND} if no object exists with the given key</li>
     *         <li>{@link SaveServiceReturnCode#OFFLINE} if the service is disabled</li>
     *         <li>{@link SaveServiceReturnCode#INVALID_KEY} if the key is null or blank</li>
     *         <li>{@link SaveServiceReturnCode#SQL_ERROR} if a database error occurred</li>
     *         </ul>
     * @throws NullPointerException if emptyInstance is null
     */
    <T extends SaveableObject> CompletableFuture<SaveServiceReturnCode> load(String key, T emptyInstance);

    /**
     * Deletes an object from persistent storage using the specified key.
     *
     * <p>This operation is asynchronous and will remove the object from both
     * the cache and the database.
     *
     * @param key the unique identifier for the object to delete, must not be null or blank
     * @return a {@link CompletableFuture} that completes with:
     *         <ul>
     *         <li>{@link SaveServiceReturnCode#SUCCESS} if the object was deleted successfully</li>
     *         <li>{@link SaveServiceReturnCode#KEY_NOT_FOUND} if no object exists with the given key</li>
     *         <li>{@link SaveServiceReturnCode#OFFLINE} if the service is disabled</li>
     *         <li>{@link SaveServiceReturnCode#INVALID_KEY} if the key is null or blank</li>
     *         <li>{@link SaveServiceReturnCode#SQL_ERROR} if a database error occurred</li>
     *         </ul>
     */
    CompletableFuture<SaveServiceReturnCode> delete(String key);

    /**
     * Checks if an object exists in persistent storage or cache using the specified key.
     *
     * <p>This operation is asynchronous and will first check the cache before
     * querying the database if necessary.
     *
     * @param key the unique identifier to check for existence, must not be null or blank
     * @return a {@link CompletableFuture} that completes with:
     *         <ul>
     *         <li>{@link SaveServiceReturnCode#EXISTS} if an object exists with the given key</li>
     *         <li>{@link SaveServiceReturnCode#NOT_EXISTS} if no object exists with the given key</li>
     *         <li>{@link SaveServiceReturnCode#OFFLINE} if the service is disabled</li>
     *         <li>{@link SaveServiceReturnCode#INVALID_KEY} if the key is null or blank</li>
     *         <li>{@link SaveServiceReturnCode#SQL_ERROR} if a database error occurred</li>
     *         </ul>
     */
    CompletableFuture<SaveServiceReturnCode> exists(String key);
}
