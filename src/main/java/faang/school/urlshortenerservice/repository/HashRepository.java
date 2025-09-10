package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Интерфейс для сохранения, получения хэшей и получения уникальных чисел для хэширования
 *
 * @author Linempy
 * @since 10.09.2025
 */
public interface HashRepository extends JpaRepository<Hash, String> {

    @Query(nativeQuery = true, value = """
            SELECT nextval('unique_number_seq') FROM generate_series(1, :range)
            """)
    List<Long> getUniqueNumbers(@Param("range") int range);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM hash
            WHERE hash IN (
                SELECT hash FROM hash
                LIMIT :count
                FOR UPDATE SKIP LOCKED
            )
            RETURNING hash
            """)
    List<String> getHashBatch(@Param("count") int count);

}