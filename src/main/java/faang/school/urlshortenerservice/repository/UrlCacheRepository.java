package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {

    private final StringRedisTemplate redis;

    public void save(String hash, String url) {
        redis.opsForValue().set(hash, url);
    }

    public String get(String hash) {
        return redis.opsForValue().get(hash);
    }
}
