package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.ShortUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisUrlCacheService {

    private final static String SHORT_URL_PREFIX = "short_url";
    private final RedisTemplate<String, ShortUrl> shortUrlRedisTemplate;

    @Value(value = "${data.redis.default-ttl-seconds:3600}")
    private long defaultTtlSeconds;

    public void cacheUrl(ShortUrl shortUrl) {
        cacheUrl(shortUrl, defaultTtlSeconds);
    }

    public Optional<ShortUrl> getUrl(String hash) {
        String key = getKey(hash);
        ShortUrl shortUrl = shortUrlRedisTemplate.opsForValue()
                .getAndExpire(key, Duration.ofSeconds(defaultTtlSeconds));

        return Optional.ofNullable(shortUrl);
    }

    public void deleteUrlsFromCache(List<String> hash) {
        List<String> keys = hash.stream()
                .map(this::getKey)
                .toList();
        shortUrlRedisTemplate.delete(keys);
    }

    void cacheUrl(ShortUrl shortUrl, long ttlSeconds) {
        String key = getKey(shortUrl.getHash());
        shortUrlRedisTemplate.opsForValue()
                .set(key, shortUrl, Duration.ofSeconds(ttlSeconds));
    }

    private String getKey(String hash) {
        return SHORT_URL_PREFIX + ":" + hash;
    }
}
