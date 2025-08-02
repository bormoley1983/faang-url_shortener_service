package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UrlCacheRepository {
    private final StringRedisTemplate redisTemplate;

    @Value("${redis.cache.ttl.hash-hours}")
    private long ttlHours;

    public String getLongUrl(String hash) {
        String value = redisTemplate.opsForValue().get(hash);
        log.info("Redis GET [{}] = {}", hash, value);
        return value;
    }

    public void cacheLongUrl(String hash, String url) {
        redisTemplate.opsForValue().set(hash, url, ttlHours);
    }
}
