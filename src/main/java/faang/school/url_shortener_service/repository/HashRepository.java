package faang.school.url_shortener_service.repository;

import faang.school.url_shortener_service.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, String> {
    @Modifying
    @Query(value = """
            DELETE FROM hash\s
            WHERE ctid = ANY(
                ARRAY(
                    SELECT ctid\s
                    FROM hash\s
                    ORDER BY hash\s
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                )
            )
            RETURNING hash
            """, nativeQuery = true)
    List<Hash> deleteAndReturnFirstN(@Param("limit") int limit);

    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_hash_number_seq') FROM generate_series(1, :maxRange)
            """)
    List<Long> getNextRange(@Param("maxRange") long maxRange);
}