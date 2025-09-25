package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * UrlCacheRepositoryImpl — репозитоорий для работы с Redis.
 * <p>
 * Он сохраняет и получает URL и его HASH
 * </p>*
 *
 * @author andreyFomchenko
 * @since 17.09.2025
 */
@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void save(String url, String hash) {
        redisTemplate.opsForValue().set(url, hash);
    }

    @Override
    public String get(String hash) {
        return redisTemplate.opsForValue().get(hash);
    }
}
