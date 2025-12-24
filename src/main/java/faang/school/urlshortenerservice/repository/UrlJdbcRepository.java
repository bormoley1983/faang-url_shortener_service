package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UrlJdbcRepository implements UrlRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<String> deleteOldUrlsAndReturnHashes(LocalDateTime olderThan) {
        log.debug("Deleting URLs older than {}", olderThan);

        String sql = """
                DELETE FROM url
                WHERE created_at < ?
                RETURNING hash
                """;

        List<String> hashes = jdbcTemplate.queryForList(sql, String.class, olderThan);

        log.debug("Deleted {} old URLs", hashes.size());

        return hashes;
    }

    @Override
    public Optional<String> findUrlByHash(String hash) {
        String sql = "SELECT url FROM url WHERE hash = ?";

        try {
            String url = jdbcTemplate.queryForObject(sql, String.class, hash);
            return Optional.ofNullable(url);
        } catch (EmptyResultDataAccessException e) {
            log.debug("URL not found for hash: {}", hash);
            return Optional.empty();
        }
    }

    @Override
    public void save(String hash, String url) {
        log.debug("Saving URL association: hash={}", hash);

        String sql = "INSERT INTO url (hash, url) VALUES (?, ?)";

        jdbcTemplate.update(sql, hash, url);

        log.debug("Successfully saved URL association for hash: {}", hash);
    }
}
