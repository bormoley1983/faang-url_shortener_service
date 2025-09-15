package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM urls
            WHERE last_requested_at <= :expirationDate
                AND request_count < :minRequestCount
            RETURNING hash
            """)
    List<String> cleanExpiredAndGetHashes(LocalDateTime expirationDate, long minRequestCount);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE urls
            SET last_requested_at = NOW(),
                request_count = request_count + 1
            WHERE hash = :hash
            """)
    void refreshActivity(String hash);

}
