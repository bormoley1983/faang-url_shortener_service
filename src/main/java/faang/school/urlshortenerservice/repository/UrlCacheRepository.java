package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {

    private final StringRedisTemplate redis;

    public void save(String hash, String url) {
        redis.opsForValue().set(hash, url, Duration.ofDays(1));
    }

    public String get(String hash) {
        return redis.opsForValue().get(hash);
    }
}
