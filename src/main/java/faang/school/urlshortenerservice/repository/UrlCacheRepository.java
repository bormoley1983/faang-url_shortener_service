package faang.school.urlshortenerservice.repository;

import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с кэшем коротких ссылок в Redis.
 * <p>
 * Отвечает за быстрое сохранение и получение ассоциаций
 * hash → длинная URL для ускорения редиректов.
 * </p>
 *
 * @author agent
 * @since 12.09.2025
 */
@Repository
public interface UrlCacheRepository {
    void save(String hash, String url);

    String get(String hash);
}