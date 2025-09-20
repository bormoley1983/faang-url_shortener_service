package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UrlRepository — репозиторий для получение hash для url.
 * <p>
 * Прозводит поиск URL по hash
 * </p>*
 *
 * @author andreyFomchenko
 * @since 17.09.2025
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
