package faang.school.urlshortenerservice.repository.redis;

import faang.school.urlshortenerservice.config.UrlCacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final UrlCacheProperties props;

    @Override
    public void save(String hash, String longUrl) {
        stringRedisTemplate.opsForValue()
                .set(buildKey(hash), longUrl, props.getTtl());
    }

    private String buildKey(String hash) {
        return "urlshortener:" + props.getVersion() + ":url:" + hash;
    }
}
