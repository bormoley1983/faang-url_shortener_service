package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<ShortUrl, String> {
    Optional<ShortUrl> findByHash(String hash);

    @Query(nativeQuery = true, value = """
            SELECT hash FROM url
            WHERE expire_time < NOW()
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """)
    List<String> findExpiredUrlHashes(@Param("limir") int limit);
}
