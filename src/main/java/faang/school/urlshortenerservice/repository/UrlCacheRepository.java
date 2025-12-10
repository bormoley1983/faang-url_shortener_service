package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class UrlCacheRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String URL_KEY_PREFIX = "url:";
    private static final Duration TTL = Duration.ofDays(7);

    public void cacheUrl(String hash, String originalUrl) {
        String key = URL_KEY_PREFIX + hash;
        try {
            redisTemplate.opsForValue().set(key, originalUrl, TTL);
            log.debug("Cached URL: hash: {} original url: {}", hash, originalUrl);
        } catch (Exception e) {
            log.error("Failed to cache URL {}: {}", hash, e.getMessage());
        }
    }

    public Optional<String> getCachedUrl(String hash) {
        String key = URL_KEY_PREFIX + hash;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Optional.of(value.toString()) : Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get cached URL {}: {}", hash, e.getMessage());
            return Optional.empty();
        }
    }
}