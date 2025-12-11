package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HashRepository {
    private final JdbcTemplate jdbcTemplate;

    @Value("${url-shortener.batch-size}")
    private int batchSize;

    private static final int JDBC_BATCH_SIZE = 50;

    public List<Long> getUniqueNumbers(int n) {
        String sql = """
                SELECT nextval('unique_number_seq') 
                FROM generate_series(1, ?)
                """;
        return jdbcTemplate.queryForList(sql, Long.class, n);
    }

    public void save(List<String> hashes) {
        String sql = "INSERT INTO hash (hash) VALUES (?)";

        jdbcTemplate.batchUpdate(sql, hashes, JDBC_BATCH_SIZE,
                (ps, hash) -> ps.setString(1, hash));
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
}
