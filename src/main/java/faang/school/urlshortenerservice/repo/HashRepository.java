package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Hash;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

  //  save(hashes) //сохраняет список хэшей батчом (или батчами), а не каждый хэш отдельным запросом, в таблицу hash.
}
