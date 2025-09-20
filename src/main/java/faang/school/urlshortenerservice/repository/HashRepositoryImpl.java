package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@RequiredArgsConstructor
public class HashRepositoryImpl implements HashRepositoryUtil {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Long> getUniqueNumbers(int n) {
        String sql = "SELECT nextval('unique_number_seq') FROM generate_series(1, ?)";
        return jdbcTemplate.queryForList(sql, Long.class, n);
    }

    @Override
    public void save(List<String> hashes) {
        String sql = "INSERT INTO hash (hash) VALUES (?)";
        jdbcTemplate.batchUpdate(sql, hashes, hashes.size(),
                (ps, hash) -> ps.setString(1, hash));
    }

    @Override
    public List<String> getHashBatch(int n) {
        String sql = "DELETE FROM hash WHERE hash IN (SELECT hash FROM hash ORDER BY hash asc LIMIT ?) RETURNING *";
        return jdbcTemplate.queryForList(sql, String.class, n);
    }
}
