package faang.school.urlshortenerservice.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(String hash, String longUrl) {
        stringRedisTemplate.opsForValue()
                .set(buildKey(hash), longUrl);
    }

    private String buildKey(String hash) {
        return "url:" + hash;
    }
}
