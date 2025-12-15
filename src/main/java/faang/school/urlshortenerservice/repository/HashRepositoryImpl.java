package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.config.hash.HashProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HashRepositoryImpl implements HashRepository {

    private final JdbcTemplate jdbcTemplate;
    private final HashProperties hashProperties;

    @Override
    public List<Long> getUniqueNumbers(int n) {
        String sql = """
                SELECT nextval('unique_number_seq')
                FROM generate_series(1, ?)
                """;
        return jdbcTemplate.queryForList(sql, Long.class, n);
    }

    @Override
    public void save(List<String> hashes) {
        String sql = """
                INSERT INTO hash (hash) VALUES (?)
                ON CONFLICT (hash) DO NOTHING;
                """;
        jdbcTemplate.batchUpdate(sql, hashes, hashProperties.getBatchSize(),
                (ps, hash) -> ps.setString(1, hash));
    }

    @Override
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
        return jdbcTemplate.queryForList(sql, String.class, hashProperties.getBatchSize());
    }
}
