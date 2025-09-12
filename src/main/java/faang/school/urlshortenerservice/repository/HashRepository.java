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

    /**
     * Атомарно получает и удаляет указанное количество случайных хэшей
     * с помощью RETURNING (PostgreSQL).
     */
    @Modifying
    @Query(value = """
        DELETE FROM hash
        WHERE hash IN (
            SELECT hash
            FROM hash
            ORDER BY RANDOM()
            LIMIT :limit
        )
        RETURNING hash
        """, nativeQuery = true)
    List<String> getHashBatch(@Param("limit") int limit);

    @Modifying
    @Query(value = "INSERT INTO hash (hash) VALUES (:hash) ON CONFLICT DO NOTHING", nativeQuery = true)
    void saveReleasedHash(@Param("hash") String hash);

    @Modifying
    @Query(value = """
        INSERT INTO hash (hash) 
        SELECT unnest(CAST(:hashes AS text[]))
        ON CONFLICT (hash) DO NOTHING
        """, nativeQuery = true)
    void saveReleasedHashes(@Param("hashes") List<String> hashes);

    @Query(value = """
        SELECT nextval('unique_number_seq') 
        FROM generate_series(1, :count)
        """, nativeQuery = true)
    List<Long> getUniqueNumbers(@Param("count") int count);
}
