package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Hash;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UrlHashJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    @Value("${hash.batch-size:5000}")
    private int batchSize;

    public List<Long> getNextRangeSequence(int rangeSequence) {
        String sql = "SELECT nextval('unique_number_seq') FROM generate_series(1, ?)";
        return jdbcTemplate.queryForList(sql, Long.class, rangeSequence);
    }

    @Transactional
    public void batchInsertHashes(List<String> hashes) {
        String sql = "INSERT INTO hashes (hash) VALUES (?)";

        for (int i = 0; i < hashes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, hashes.size());
            List<String> batch = hashes.subList(i, end);

            jdbcTemplate.batchUpdate(sql, batch, batch.size(),
                    (ps, hash) -> ps.setString(1, hash));
        }
    }

    @Transactional
    public List<Hash> findAndDelete(long count) {
        String sql = """
                    DELETE FROM hashes
                    WHERE hash IN (
                        SELECT hash
                        FROM hashes
                        ORDER BY hash
                        LIMIT ?
                        FOR UPDATE SKIP LOCKED
                    )
                    RETURNING hash
                """;

        return jdbcTemplate.query(
                sql,
                ps -> ps.setLong(1, count),
                (rs, rowNum) -> new Hash(rs.getString("hash"))
        );
    }
}