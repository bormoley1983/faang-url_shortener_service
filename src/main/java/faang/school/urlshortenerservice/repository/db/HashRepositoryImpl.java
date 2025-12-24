package faang.school.urlshortenerservice.repository.db;

import faang.school.urlshortenerservice.config.hash.HashProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HashRepositoryImpl implements HashRepository {

    private final JdbcTemplate jdbcTemplate;
    private final HashProperties hashProperties;

    @Override
    public List<Long> getUniqueNumbers(int count) {
        String sql = """
                SELECT nextval('unique_number_seq')
                FROM generate_series(1, ?)
                """;
        return jdbcTemplate.queryForList(sql, Long.class, count);
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
    @Transactional
    public List<String> getHashBatch(int batchSize) {
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
