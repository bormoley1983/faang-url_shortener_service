package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.ShortUrl;
import faang.school.urlshortenerservice.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisUrlCacheService {

    private final static String SHORT_URL_PREFIX = "short_url";
    private final RedisTemplate<String, ShortUrl> redisTemplate;

    @Value(value = "${data.redis.default-ttl-seconds:3600}")
    private long defaultTtlSeconds;

    public void cacheUrl(ShortUrl shortUrl) {
        cacheUrl(shortUrl, defaultTtlSeconds);
    }

    public Optional<ShortUrl> getUrl(String hash) {
        String key = getKey(hash);
        ShortUrl shortUrl = redisTemplate.opsForValue()
                .getAndExpire(key, Duration.ofSeconds(defaultTtlSeconds));

        return Optional.ofNullable(shortUrl);
    }

    public void deleteUrlsFromCache(List<String> hash) {
        List<String> keys = hash.stream()
                .map(this::getKey)
                .toList();
        redisTemplate.delete(keys);
    }

    void cacheUrl(ShortUrl shortUrl, long ttlSeconds) {
        String key = getKey(shortUrl.getHash());
        redisTemplate.opsForValue()
                .set(key, shortUrl, Duration.ofSeconds(ttlSeconds));
    }

    private String getKey(String hash) {
        return SHORT_URL_PREFIX + ":" + hash;
    }
}
