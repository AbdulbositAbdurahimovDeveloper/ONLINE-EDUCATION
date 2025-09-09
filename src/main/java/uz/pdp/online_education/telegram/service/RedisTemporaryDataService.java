package uz.pdp.online_education.telegram.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service abstraction for managing temporary, multi-field data in Redis.
 * <p>
 * This service is designed for handling short-lived, multistep processes
 * (e.g., conversational flows in a Telegram bot) using Redis Hashes combined
 * with Time-To-Live (TTL) expiration.
 * </p>
 *
 * <p>Typical use cases include:</p>
 * <ul>
 *     <li>Storing in-progress form submissions or wizards</li>
 *     <li>Managing multistep user interactions with partial state persistence</li>
 *     <li>Ensuring temporary data automatically expires without manual cleanup</li>
 * </ul>
 */
public interface RedisTemporaryDataService {

    /**
     * Starts a new temporary process with a default TTL (Time-To-Live).
     * <p>
     * The default TTL value is configured in {@code application.yml} under
     * {@code application.cache.temporary-process-ttl-seconds}.
     * </p>
     *
     * @param key         Unique Redis key for the process (e.g.,
     *                    {@code "module_create:chatId:12345:courseId:99"}).
     * @param initialData Initial fields to store at process start
     *                    (e.g., a courseId or metadata).
     */
    void startProcess(String key, Map<String, Object> initialData);

    /**
     * Starts a new temporary process with a custom TTL.
     *
     * @param key         Unique Redis key for the process.
     * @param initialData Initial fields to store at process start.
     * @param ttlSeconds  Expiration time in seconds. Once expired,
     *                    all process-related data will be discarded automatically.
     */
    void startProcess(String key, Map<String, Object> initialData, long ttlSeconds);

    /**
     * Adds or updates a single field within an existing process.
     * <p>
     * If the key or field does not exist, it will be created. If it exists,
     * the value will be updated.
     * </p>
     *
     * @param key   Redis key of the process.
     * @param field Field name to add or update (e.g., {@code "title"}).
     * @param value Field value.
     */
    void addField(String key, String field, Object value);

    /**
     * Retrieves the values of specific fields from an existing process.
     * <p>
     * Fields that are not found in Redis will be excluded from the result map.
     * </p>
     *
     * @param key    Redis key of the process.
     * @param fields List of field names to fetch
     *               (e.g., {@code ["title", "description"]}).
     * @return A map of found fields and their corresponding values.
     */
    Map<String, Object> getFields(String key, List<String> fields);

    /**
     * Retrieves all fields and their values for an existing process.
     *
     * @param key Redis key of the process.
     * @return An {@link Optional} containing all fields as a map if the process exists,
     *         or {@link Optional#empty()} if the key does not exist or has expired.
     */
    Optional<Map<String, Object>> getAllFields(String key);

    /**
     * Explicitly terminates a process by deleting all related data from Redis.
     * <p>
     * This provides a way to clean up resources immediately without waiting
     * for TTL expiration.
     * </p>
     *
     * @param key Redis key of the process to remove.
     */
    void endProcess(String key);
}
