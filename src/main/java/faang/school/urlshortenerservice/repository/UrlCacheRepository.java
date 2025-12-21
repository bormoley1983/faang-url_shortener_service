package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * Репозиторий для кэширования ассоциаций url - cache в redis на определенное в конфиге количество дней.
 * Цель: основная активность новой ссылки приходится на ближайшее после её создание время, так что
 * будет логично кэшировать ее на срок >= 1 день для более быстрого отклика.
 */
@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;
    @Value("${spring.data.redis.ttl-days}")
    private Long ttlDays;

    public void save(String hash, String url) {
        redisTemplate.opsForValue().set(hash, url, ttlDays, TimeUnit.DAYS);
    }

    public String getCacheUrl(String hash) {
        return redisTemplate.opsForValue().get(hash);
    }
}
