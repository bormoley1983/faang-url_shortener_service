package faang.school.urlshortenerservice.repository.impl;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisUrlCacheRepository implements UrlCacheRepository {

    private static final String KEY_PREFIX = "url:";
    private static final int CACHE_TTL = 24;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String findUrlByHash(Hash hash) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + hash.getHashValue());
    }

    @Override
    public void saveUrl(String hash, String originalUrl) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + hash,
                originalUrl,
                CACHE_TTL,
                TimeUnit.HOURS);
    }
}
