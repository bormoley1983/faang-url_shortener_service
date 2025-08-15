package faang.school.urlshortenerservice.repo;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UrlCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public UrlCacheRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String findByHash(String hash) {
        return redisTemplate.opsForValue().get(hash);
    }

    public void save(String hash, String originalUrl) {
        redisTemplate.opsForValue().set(hash, originalUrl);
    }
}