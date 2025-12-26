package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Hash> {

    @Query("SELECT u.url FROM Url u WHERE u.hash = :hash")
    Optional<String> findUrlByHash(@Param("hash") String hash);

    @Query(nativeQuery = true, value = """
            DELETE FROM url
               WHERE created_at < NOW() - INTERVAL '1 year'
               LIMIT: limit
               RETURNING *
            """)
    List<Url> deleteOldUrls(@Param("limit") int limit);

    @Query(nativeQuery = true, value = """
            SELECT COUNT(*) FROM url
               WHERE created_at < NOW() - INTERVAL '1 year'
            """)
    Long countOldUrls();
}