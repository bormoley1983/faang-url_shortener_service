package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.model.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с ассоциациями коротких хэшей и длинных URL.
 * <p>
 * Отвечает за сохранение и поиск пар hash → длинная URL
 * в базе данных PostgreSQL.
 * </p>
 *
 * @author agent
 * @since 10.09.2025
 */
public interface UrlRepository extends JpaRepository<UrlEntity, String> {
    UrlEntity findByHash(String hash);

    default UrlEntity findByHashOrElseThrow(String hash) {
        UrlEntity entity = findByHash(hash);
        if (entity == null) {
            throw new UrlNotFoundException("URL не найден для хэша: " + hash);
        }
        return entity;
    }
}