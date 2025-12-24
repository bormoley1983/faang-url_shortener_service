package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.properties.HashBatchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class HashJdbcRepository implements HashRepository {

    private final JdbcTemplate jdbcTemplate;
    private final HashBatchProperties hashBatchProperties;

    @Override
    public List<Long> getUniqueNumbers(Integer count) {
        log.debug("Generating {} unique numbers from sequence", count);

        String sql = """
                SELECT nextval('unique_number_seq') 
                FROM generate_series(1, ?)
                """;

        return jdbcTemplate.queryForList(sql, Long.class, count);
    }

    @Override
    public void save(List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            log.warn("Attempted to save empty hash list");
            return;
        }

        log.debug("Saving {} hashes in batch", hashes.size());

        String sql = "INSERT INTO hash (hash) VALUES (?)";

        jdbcTemplate.batchUpdate(sql, hashes, hashes.size(),
                (ps, hash) -> ps.setString(1, hash));

        log.debug("Successfully saved {} hashes", hashes.size());
    }

    @Override
    public List<String> getHashBatch() {
        Integer batchSize = hashBatchProperties.getBatchSize();
        log.debug("Fetching batch of {} random hashes", batchSize);

        String sql = """
                DELETE FROM hash
                WHERE hash IN (
                    SELECT hash 
                    FROM hash 
                    ORDER BY RANDOM()
                    LIMIT ?
                )
                RETURNING hash
                """;

        List<String> hashes = jdbcTemplate.queryForList(sql, String.class, batchSize);
        log.debug("Fetched and deleted {} hashes", hashes.size());

        return hashes;
    }
}
