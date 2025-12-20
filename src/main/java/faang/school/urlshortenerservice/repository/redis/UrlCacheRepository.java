package faang.school.urlshortenerservice.repository.redis;

import java.util.Optional;

/**
 * Repository for caching URL mappings in Redis.
 * <p>
 * Redis is used as a fast-access cache for resolving short URLs.
 * The database remains the source of truth.
 */
public interface UrlCacheRepository {

    /**
     * Stores a mapping between a short URL hash and the original URL in Redis.
     * <p>
     * The entry is stored with a predefined TTL.
     * This operation is typically best-effort: failures should not prevent
     * the application from functioning, as the data can be retrieved from the database.
     *
     * @param hash    short URL hash
     * @param longUrl original URL associated with the hash
     */
    void save(String hash, String longUrl);

    /**
     * Retrieves the original URL associated with the given hash from Redis.
     * <p>
     * If the mapping is not present in the cache or the key has expired,
     * an empty {@link Optional} is returned.
     *
     * @param hash short URL hash
     * @return an {@link Optional} containing the original URL if found in cache,
     * or {@link Optional#empty()} if not found
     */
    Optional<String> find(String hash);
}