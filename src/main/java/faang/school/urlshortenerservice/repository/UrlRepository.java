package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UrlRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<String> getOldUrlHashes(LocalDateTime cutoffDate) {
        String sql = """
                SELECT hash FROM url
                WHERE created_at < ?
                """;
        return jdbcTemplate.queryForList(sql, String.class, cutoffDate);
    }

    public List<String> getOldUrlHashesBatch(LocalDateTime cutoffDate, int limit) {
        String sql = """
                SELECT hash FROM url
                WHERE created_at < ?
                LIMIT ?
                """;
        return jdbcTemplate.queryForList(sql, String.class, cutoffDate, limit);
    }

    public void deleteUrlsByHashes(List<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return;
        }
        jdbcTemplate.execute((Connection connection) -> {
            String sql = "DELETE FROM url WHERE hash = ANY(?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                java.sql.Array array = connection.createArrayOf("VARCHAR", hashes.toArray());
                ps.setArray(1, array);
                return ps.executeUpdate();
            }
        });
    }

    @Deprecated
    public List<String> deleteOldUrlsAndReturnHashes(LocalDateTime cutoffDate) {
        String sql = """
                DELETE FROM url
                WHERE created_at < ?
                RETURNING hash
                """;
        return jdbcTemplate.queryForList(sql, String.class, cutoffDate);
    }
}