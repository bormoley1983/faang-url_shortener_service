package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Url;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class UrlCashRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String hash, Url url, long ttlDays) {
        redisTemplate.opsForValue().set(hash, url, Duration.ofDays(ttlDays));
    }

    public Url get(String hash) {
        Object obj = redisTemplate.opsForValue().get(hash);
        return obj instanceof Url ? (Url) obj : null;
    }

    public void delete(String hash) {
        redisTemplate.delete(hash);
    }
}