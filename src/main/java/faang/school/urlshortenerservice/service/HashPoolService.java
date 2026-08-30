package faang.school.urlshortenerservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HashPoolService {
    private static final String TAKE_BATCH_SQL = """
            DELETE FROM hash
            WHERE hash IN (SELECT hash FROM hash ORDER BY RANDOM() LIMIT ?)
            RETURNING hash
            """;

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public List<String> takeBatch(int batchSize) {
        return jdbcTemplate.queryForList(TAKE_BATCH_SQL, String.class, batchSize);
    }
}
