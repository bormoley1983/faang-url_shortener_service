package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.UrlEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для SQL-запросов для сущности {@link UrlEntity}
 *
 * @author Linempy
 * @since 13.09.2025
 */
public interface UrlRepository extends JpaRepository<UrlEntity, String> {

    boolean existsByUrl(String url);

    Optional<UrlEntity> findByUrl(String originalUrl);

    default UrlEntity findByUrlOrThrows(String originalUrl) {
        return findByUrl(originalUrl)
                .orElseThrow(() -> new EntityNotFoundException("Данного URL нет в базе данных"));
    }

    default UrlEntity findByIdOrThrows(String hash) {
        return findById(hash)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                String.format("Данного URL с хэшем: %s нет в базе данных", hash)
                        )
                );
    }

    @Query(nativeQuery = true, value = """
            DELETE FROM urls
            WHERE created_at < NOW() - INTERVAL :periodCleanUp YEAR
            RETURNING hash
            """)
    List<String> deleteOldHashesAndReturn(@Param("periodCleanUp") int periodCleanUp);
}