package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Реализация репозитория для работы с кэшем коротких ссылок в Redis.
 * <p>
 * Отвечает за быстрое сохранение и получение ассоциаций
 * hash → длинная URL для ускорения редиректов.
 * Использует {@link RedisTemplate} для взаимодействия с Redis.
 * </p>
 *
 * @author agent
 * @since 12.09.2025
 */
@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Сохраняет пару hash → url в Redis.
     *
     * @param hash короткий хэш ссылки
     * @param url  длинная URL
     */
    @Override
    public void save(String hash, String url) {
        redisTemplate.opsForValue().set(hash, url);
    }

    /**
     * Получает длинную URL по короткому хэшу из Redis.
     *
     * @param hash короткий хэш
     * @return длинная URL или null, если хэш не найден
     */
    @Override
    public String get(String hash) {
        return redisTemplate.opsForValue().get(hash);
    }
}