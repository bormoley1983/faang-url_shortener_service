package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cache.url.prefix:url:redirect:v1:}")
    private String keyPrefix;

    public void saveUrl(String hash, String originalUrl, Duration ttl) {
        redisTemplate.opsForValue().set(buildKey(hash), originalUrl, ttl);
    }

    public String getUrl(String hash) {
        return redisTemplate.opsForValue().get(buildKey(hash));
    }

    public boolean containsUrl(String hash) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(hash)));
    }

    public void deleteByHash(String hash) {
        redisTemplate.delete(buildKey(hash));
    }

    private String buildKey(String hash) {
        return keyPrefix + hash;
    }
}
