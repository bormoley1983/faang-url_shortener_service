package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlRepository {
    private final JdbcTemplate jdbcTemplate;

    public Optional<String> findUrl(String hash) {
        String sql = "SELECT url FROM url WHERE hash = ?";
        try {
            String url = jdbcTemplate.queryForObject(sql, String.class, hash);
            return Optional.ofNullable(url);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void save(String hash, String url) {
        String sql = "INSERT INTO url (hash, url) VALUES (?, ?)";
        jdbcTemplate.update(sql, hash, url);
    }

    public List<String> deleteOldUrlsAndReturnHashes(LocalDateTime oldDate) {
        String sql = """
                DELETE FROM url
                WHERE created_at < ?
                RETURNING hash
                """;
        return jdbcTemplate.queryForList(sql, String.class, oldDate);
    }
}