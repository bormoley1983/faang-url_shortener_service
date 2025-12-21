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

    @Query(
            value = "SELECT nextval('unique_number_seq') FROM generate_series(1, :n)",
            nativeQuery = true
    )
    List<Long> getUniqueNumbers(@Param("n") int n);

    @Modifying
    @Query(value = """
            WITH deleted AS (
                DELETE FROM hash
                WHERE ctid IN (
                    SELECT ctid
                    FROM hash
                    LIMIT :n
                    FOR UPDATE SKIP LOCKED
                )
                RETURNING hash
            )
            SELECT hash FROM deleted;
            """, nativeQuery = true)
    List<String> getHashBatch(@Param("n") int n);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            INSERT INTO hash (hash)
            SELECT unnest(cast(:hashes AS text[]))
            ON CONFLICT (hash) DO NOTHING;
            """, nativeQuery = true)
    void returnHashes(@Param("hashes") String[] hashes);
}
