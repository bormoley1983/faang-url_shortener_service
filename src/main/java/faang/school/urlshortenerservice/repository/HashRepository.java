package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, String> {
    @Query(nativeQuery = true, value = "SELECT p_try_advisory_lock(:lockId)")
    boolean tryLock (@Param("lockId") int lockId);

    @Query(nativeQuery = true, value = "SELECT pg_advisory_unlock(:lockId)")
    void unLock (@Param("lockId") int lockId);

    @Query(nativeQuery = true, value = """
            DELETE FROM free_hash_storage
            WHERE hash IN (
                SELECT hash FROM free_hash_storage
                FOR UPDATE SKIP LOCKED
                LIMIT: batchSize
                ) RETURNING hash
            """)
    List<String> getFreeHashesBatchWithLockAndDelete (long batchSize);

    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_number_seq')
            FROM generated_series(1, :count)
            """)
    List<Long> getNextSequenseBatch(@Param("count") long count);
}
