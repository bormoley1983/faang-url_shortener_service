package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashRepository extends JpaRepository<Hash, Long> {

    @Query(nativeQuery = true, value = """
            SELECT nextval ('unique_hash_number_seq') FROM generate_series(1, :maxRange)""")
    List<Long> getUniqueNumbers(int maxRange);

    @Query(nativeQuery = true, value = """
            DELETE from hash WHERE id IN(SELECT id FROM hash ORDER BY id ASC LIMIT :amount) RETURNING *""")
    List<Hash> getAndDelete(long number);

    @Query(nativeQuery = true, value = """
            DELETE FROM hash 
            WHERE id = (SELECT id FROM hash ORDER BY id ASC LIMIT 1)
            RETURNING *""")
    Optional<Hash> getAndDeleteOne();

    @Query(nativeQuery = true, value = """
            DELETE FROM hash 
            WHERE id IN (SELECT id FROM hash ORDER BY id ASC LIMIT :amount)
            RETURNING *""")
    List<Hash> getAndDeleteHashBatch(long amount);
}
