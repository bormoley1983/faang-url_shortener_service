package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, String> {
    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_numbers_seq')
            FROM generate_series(1, :batchSize)
            """)
    List<Long> getUniqueNumbers(int batchSize);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM hashes
            WHERE hash IN (
                SELECT hash
                FROM hashes
                LIMIT :batchSize
            ) RETURNING hash
            """)
    List<String> getHashBatch(int batchSize);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO hashes(hash)
                SELECT hash
                FROM unnest(:hashes) AS hash
            """)
    void saveBatch(String[] hashes);
}
