package faang.school.urlshortenerservice.service;

/**
 * Thread-safe cache for storing and providing free hashes.
 * <p>
 * Provides non-blocking access to hashes and triggers asynchronous
 * cache refill when the number of cached hashes drops below a configured threshold.
 */
public interface HashCache {

    /**
     * Returns a single free hash from the cache.
     * <p>
     * The method is non-blocking.
     * If the cache size falls below the configured threshold,
     * an asynchronous refill is triggered exactly once.
     *
     * @return free hash value or {@code null} if cache is temporarily empty
     */
    String getHash();
}