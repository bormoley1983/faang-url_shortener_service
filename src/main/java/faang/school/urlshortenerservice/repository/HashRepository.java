package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends CrudRepository<Hash, String> {

    @Query(nativeQuery = true,
            value = """
                    SELECT nextval('unique_number_seq')
                        FROM generate_series(1, :n)
                    """)
    List<Long> getUniqueNumbers(long n);

    void saveAll(List<Hash> hashes);

    @Modifying
    @Query(nativeQuery = true,
            value = """
                    DELETE FROM hashes
                                WHERE hash IN (
                                    SELECT hash
                                    FROM hashes
                                    ORDER BY hash
                                    LIMIT :n
                                )
                                RETURNING *
                    """)
    List<Hash> deleteHashBatch(long n);
}
