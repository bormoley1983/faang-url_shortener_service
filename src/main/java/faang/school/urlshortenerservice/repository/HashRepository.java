package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, String> {

    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_hash_number_seq') FROM generate_series(1, :maxRange)
            """)
    List<Long> getNextRange(@Param("maxRange") long maxRange);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM hash
                    WHERE ctid IN (
                        SELECT ctid
                        FROM hash
                        LIMIT :hashLimit
                    )
                    RETURNING hash
            """)
    List<Hash> findAndDelete(@Param("hashLimit") long hashLimit);
}