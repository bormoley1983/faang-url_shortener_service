package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, String> {


    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_number_seq') FROM generate_series(1, :maxRange)
            """)
    List<Long> getNextRange(@Param("maxRange") long maxRange);

    @Modifying
    @Query(value = """
        WITH deleted AS (
            DELETE FROM hash 
            WHERE ctid IN (
                SELECT ctid 
                FROM hash 
                ORDER BY hash 
                LIMIT :limit
            )
            RETURNING hash
        )
        SELECT * FROM deleted
        """, nativeQuery = true)
    List<Hash> deleteAndReturnFirstN(@Param("limit") int limit);

    @Query(value = "SELECT COUNT(*) FROM hash", nativeQuery = true)
    Long countTotal();
}
