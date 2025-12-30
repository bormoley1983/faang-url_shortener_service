package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Url;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortUrl(@NotBlank String shortUrl);

    @Query(nativeQuery = true, value = """
            DELETE FROM url
            WHERE id IN (
                SELECT id
                FROM url
                WHERE created_at < :threshold
                ORDER BY id
                LIMIT :batchSize
            )
            RETURNING hash""")
    List<String> deleteExpiredAndReturnHashes(@Param("threshold") OffsetDateTime threshhold,
                                              @Param("batchSize") int batchSize);
}
