package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${url-shortener.cache.ttl-hours:24}")
    private int ttlHours;

    private static final String URL_CACHE_PREFIX = "url:";

    /**
     * Сохраняет ассоциацию хэш -> оригинальный URL в Redis
     *
     * @param hash хэш короткой ссылки
     * @param originalUrl оригинальный URL
     */
    public void save(String hash, String originalUrl) {
        String key = URL_CACHE_PREFIX + hash;
        redisTemplate.opsForValue().set(key, originalUrl, Duration.ofHours(ttlHours));
        log.debug("Saved to Redis cache: {} -> {} with TTL {} hours", key, originalUrl, ttlHours);
    }

    /**
     * Получает оригинальный URL по хэшу из Redis
     *
     * @param hash хэш короткой ссылки
     * @return Optional с оригинальным URL или empty если не найден
     */
    public Optional<String> findByHash(String hash) {
        String key = URL_CACHE_PREFIX + hash;
        String originalUrl = redisTemplate.opsForValue().get(key);

        if (originalUrl != null) {
            log.debug("Found in Redis cache: {} -> {}", key, originalUrl);
            return Optional.of(originalUrl);
        } else {
            log.debug("Not found in Redis cache: {}", key);
            return Optional.empty();
        }
    }

    /**
     * Удаляет ассоциацию из Redis по хэшу
     *
     * @param hash хэш короткой ссылки
     */
    public void deleteByHash(String hash) {
        String key = URL_CACHE_PREFIX + hash;
        Boolean deleted = redisTemplate.delete(key);
        log.debug("Deleted from Redis cache: {}, result: {}", key, deleted);
    }
}