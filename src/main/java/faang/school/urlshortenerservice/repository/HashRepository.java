package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class HashRepository {

    private final JdbcTemplate jdbcTemplate;

    @Value("${hash.batch-size}")
    private int batchSize;


    public List<Long> getUniqueNumbers(int num) {
        String sql = "SELECT nextval('unique_number_seq') FROM generate_series(1, ?)";
        return jdbcTemplate.queryForList(sql, Long.class, num);
    }

    public void save(List<String> hashes) {
        if(hashes == null || hashes.isEmpty()) {return;}
        jdbcTemplate.batchUpdate(
                "INSERT INTO hash (hash) VALUES (?) ON CONFLICT (hash) DO NOTHING",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, hashes.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return hashes.size();
                    }
                }
        );
    }

    public List<String> getHashBatch() {
        String sql = """
                DELETE FROM hash
                WHERE hash IN (
                    SELECT hash FROM hash
                    ORDER BY random()
                    LIMIT ?
                )
                RETURNING hash
                """;
        return jdbcTemplate.queryForList(sql, String.class, batchSize);
    }
}