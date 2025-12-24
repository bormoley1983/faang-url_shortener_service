package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Url;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с таблицей url. Имеет методы для получения url по хэшу и
 * удаления батча устаревших ассоциаций url - hash
 */
public interface UrlRepository extends JpaRepository<Url, String> {

    @Query("SELECT u.url FROM Url u WHERE u.hash = :hash")
    Optional<String> findUrlByHash(@Param("hash") String hash);

    @Modifying
    @Query(nativeQuery = true, value = """
                DELETE FROM url WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '1 YEAR'
            """)
    List<String> deleteOldHashes();

    default String findUrlByHashOrElseThrow(String hash) {
        return findUrlByHash(hash)
                .orElseThrow(() -> new EntityNotFoundException("Не был найден url с хэшом " + hash));
    }
}
