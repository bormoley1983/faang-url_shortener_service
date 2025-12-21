package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.dto.URLCacheData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisURLCacheRepository {
    private static final String CACHE_KEY_PREFIX = "url:";
    private static final long CACHE_TTL_HOURS = 24;

    private final RedisTemplate<String, URLCacheData> redisTemplate;

    public RedisURLCacheRepository(RedisTemplate<String, URLCacheData> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String hash, String originalUrl) {
        URLCacheData data = URLCacheData.builder()
                .hash(hash)
                .originalUrl(originalUrl)
                .build();
        redisTemplate.opsForValue().set(
                CACHE_KEY_PREFIX + hash,
                data,
                CACHE_TTL_HOURS,
                TimeUnit.HOURS
        );
    }

    public Optional<String> getByHash(String hash) {
        URLCacheData data = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + hash);
        return data != null ? Optional.of(data.getOriginalUrl()) : Optional.empty();
    }

    public void delete(String hash) {
        redisTemplate.delete(CACHE_KEY_PREFIX + hash);
    }
}
