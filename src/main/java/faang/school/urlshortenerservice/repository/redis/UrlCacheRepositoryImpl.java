package faang.school.urlshortenerservice.repository.redis;

import faang.school.urlshortenerservice.config.UrlCacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {

    /**
     * Redis template for working with String keys and values.
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Cache configuration properties (TTL, key version).
     */
    private final UrlCacheProperties props;

    /**
     * Stores a short URL hash to original URL mapping in Redis with TTL.
     * <p>
     * This method is typically used for cache warm-up after a successful
     * database lookup or after creating a new short URL.
     *
     * @param hash    short URL hash
     * @param longUrl original URL
     */
    @Override
    public void save(String hash, String longUrl) {
        stringRedisTemplate.opsForValue()
                .set(buildKey(hash), longUrl, props.getTtl());
    }

    /**
     * Retrieves the original URL associated with the given hash from Redis.
     * <p>
     * If the key is not present in Redis or has expired,
     * {@link Optional#empty()} is returned.
     *
     * @param hash short URL hash
     * @return optional original URL from cache
     */
    @Override
    public Optional<String> find(String hash) {
        return Optional.ofNullable(
                stringRedisTemplate.opsForValue().get(buildKey(hash))
        );
    }

    /**
     * Builds a Redis key for storing URL mappings.
     * <p>
     * Key format:
     * {@code urlshortener:{version}:url:{hash}}
     *
     * @param hash short URL hash
     * @return fully qualified Redis key
     */
    private String buildKey(String hash) {
        return props.getModule() + ":" + props.getVersion() + ":" + props.getUrlEntity() + ":" + hash;
    }
}