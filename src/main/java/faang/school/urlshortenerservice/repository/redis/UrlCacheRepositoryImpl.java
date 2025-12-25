package faang.school.urlshortenerservice.repository.redis;

import faang.school.urlshortenerservice.config.UrlCacheProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {

    private static final String KEY_SEPARATOR = ":";

    private final StringRedisTemplate redisTemplate;
    private final UrlCacheProperties props;

    private String keyPrefix;

    @PostConstruct
    void init() {
        this.keyPrefix = String.join(
                KEY_SEPARATOR,
                props.getModule(),
                props.getVersion(),
                props.getUrlEntity()
        ) + KEY_SEPARATOR;
    }

    @Override
    public void save(String hash, String longUrl) {
        if (hash == null || hash.isBlank()) {
            return;
        }
        redisTemplate.opsForValue()
                .set(buildKey(hash), longUrl, props.getTtl());
    }

    @Override
    public Optional<String> find(String hash) {
        if (hash == null || hash.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(buildKey(hash))
        );
    }

    private String buildKey(String hash) {
        return keyPrefix + hash;
    }
}