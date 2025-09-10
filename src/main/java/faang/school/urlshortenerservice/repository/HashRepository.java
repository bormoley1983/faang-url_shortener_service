package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Интерфейс для сохранения, получения хэшей и получения уникальных чисел для хэширования
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Repository
@RequiredArgsConstructor
public class HashRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Long> getUniqueNumbers(int range) {
        String query =
            """
            SELECT nextval('unique_number_seq') FROM generate_series(1, ?)
            """;

        return jdbcTemplate.queryForList(query, Long.class, range);
    }

    public List<String> getHashBatch(int count) {
        String query = """
            DELETE FROM hash
            WHERE hash IN (
                SELECT hash FROM hash
                LIMIT ?
                FOR UPDATE SKIP LOCKED
            )
            RETURNING hash
            """;

        return jdbcTemplate.queryForList(query, String.class, count);
    }

    public void saveAll(List<String> hashes) {
        String query = """
                INSERT INTO hash (hash)
                VALUES (?)
                """;

        jdbcTemplate.batchUpdate(query,
                hashes,
                hashes.size(),
                (ps, hash) -> ps.setString(1, hash)
        );
    }

}