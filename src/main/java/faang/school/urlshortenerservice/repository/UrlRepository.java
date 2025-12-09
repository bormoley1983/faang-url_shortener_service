package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {


    @Modifying
    @Query(value = """
            WITH locked_rows AS (
            SELECT hash FROM url 
            WHERE created_at < :cutoffDate 
            FOR UPDATE SKIP LOCKED
            )
            DELETE FROM url 
            WHERE hash IN (SELECT hash FROM locked_rows)
            """, nativeQuery = true)
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
