package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Url;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX_URL = "url:";

    public void save(String hash, Url url, long ttlDays) {
        try {
            redisTemplate.opsForValue().set(PREFIX_URL + hash, url, Duration.ofDays(ttlDays));
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failure while saving hash: {}", hash, e);
        } catch (DataAccessException e) {
            log.error("Data access error while saving hash to Redis: {}", hash, e);
        }
    }

    public Url get(String hash) {
        try {
            Object obj = redisTemplate.opsForValue().get(PREFIX_URL + hash);
            return obj instanceof Url ? (Url) obj : null;
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failure while retrieving hash: {}", hash, e);
            return null;
        } catch (DataAccessException e) {
            log.error("Data access error while retrieving hash from Redis: {}", hash, e);
            return null;
        }
    }

    public void delete(String hash) {
        try {
            redisTemplate.delete(PREFIX_URL + hash);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failure while deleting hash: {}", hash, e);
        } catch (DataAccessException e) {
            log.error("Data access error while deleting hash from Redis: {}", hash, e);
        }
    }
}
