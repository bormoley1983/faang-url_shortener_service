package faang.school.urlshortenerservice.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class UrlCacheImpl implements UrlCache {
    private static final String prefix = "urls:";
    @Value("${shortener.url.cache.ttl}")
    private int ttl;
    private final StringRedisTemplate cache;

    String getKey(String hash) {
        return prefix + hash;
    }

    @Override
    public String get(String hash) {
        return cache.opsForValue().get(getKey(hash));
    }

    @Override
    public void set(String hash, String url) {
        cache.opsForValue().set(getKey(hash), url, ttl, TimeUnit.SECONDS);
    }

    @Override
    public void delete(String hash) {
        cache.opsForValue().getOperations().delete(getKey(hash));
    }

    @Override
    public void deleteAll(List<String> hashes) {
        cache.opsForValue()
                .getOperations()
                .delete(hashes.stream()
                        .map(this::getKey)
                        .toList());
    }
}
