package faang.school.urlshortenerservice.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * HashRepository — репозиторий для работы с хэшэм.
 *
 * @author bozya
 * @since 12.09.2025
 */
@Repository
@RequiredArgsConstructor
public class HashRepository {

    private final JdbcTemplate jdbcTemplate;

    @Value("${hash.batch-size:1000}")
    private int batchSize;

    @Transactional
    public List<Long> getUniqueNumber(int count) {
        String sql = "SELECT nextval('unique_number_seq') FROM generate_series(1, ?)";
        return jdbcTemplate.queryForList(sql, Long.class, count);
    }

    public List<String> getHashBatch() {
        String sql = """
            DELETE FROM hash 
            WHERE hash IN (
                SELECT hash FROM hash 
                ORDER BY RANDOM() 
                LIMIT ?
            )
            RETURNING hash
            """;

        return jdbcTemplate.queryForList(sql, String.class, batchSize);
    }

    public void saveAll(List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO hash (hash, created_at) VALUES (?, NOW())";

        jdbcTemplate.batchUpdate(sql, hashes, hashes.size(),
                (ps, hash) -> ps.setString(1, hash));
    }
}