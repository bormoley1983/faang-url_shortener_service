package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, Long> {

    @Query(nativeQuery = true, value = """
            SELECT nextval('uniq_number_seq') FROM generate_series(1, :n)
            """)
    List<Long> getUniqueNumbers(int n);

    @Query(nativeQuery = true, value = """
           WITH deleted AS (
            DELETE FROM hash
            WHERE id IN (
                SELECT id FROM hash ORDER BY id ASC LIMIT :amount
            )
            RETURNING id, hash
        )
        SELECT id, hash FROM deleted
        """)
    List<Hash> getHashBatch(int amount);
}