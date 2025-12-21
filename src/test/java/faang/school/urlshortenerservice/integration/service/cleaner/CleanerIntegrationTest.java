package faang.school.urlshortenerservice.integration.service.cleaner;

import faang.school.urlshortenerservice.service.CleanerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import faang.school.urlshortenerservice.integration.AbstractIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CleanerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CleanerService cleanerService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE url");
        jdbcTemplate.execute("TRUNCATE TABLE hash");
        jdbcTemplate.execute("ALTER SEQUENCE unique_number_seq RESTART WITH 1");
    }

    @Test
    @DisplayName("clean(): удаляет записи старше retention и возвращает их hash в таблицу hash")
    void clean_deletesExpiredUrls_andReturnsHashes() {
        OffsetDateTime oldCreatedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(370);
        OffsetDateTime freshCreatedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(10);

        jdbcTemplate.update(
                "INSERT INTO url(hash, url, created_at) VALUES (?, ?, ?)",
                "aaaaaa", "https://old.example", oldCreatedAt
        );
        jdbcTemplate.update(
                "INSERT INTO url(hash, url, created_at) VALUES (?, ?, ?)",
                "bbbbbb", "https://fresh.example", freshCreatedAt
        );

        assertThat(count("hash")).isZero();

        int deleted = cleanerService.clean();

        assertThat(deleted).isEqualTo(1);

        assertThat(existsUrlHash("aaaaaa")).isFalse();
        assertThat(existsUrlHash("bbbbbb")).isTrue();

        assertThat(existsHash("aaaaaa")).isTrue();
        assertThat(count("hash")).isEqualTo(1);
    }

    private long count(String table) {
        Long n = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Long.class);
        return n == null ? 0 : n;
    }

    private boolean existsUrlHash(String hash) {
        List<String> rows = jdbcTemplate.queryForList(
                "SELECT hash FROM url WHERE hash = ?",
                String.class,
                hash
        );
        return !rows.isEmpty();
    }

    private boolean existsHash(String hash) {
        List<String> rows = jdbcTemplate.queryForList(
                "SELECT hash FROM hash WHERE hash = ?",
                String.class,
                hash
        );
        return !rows.isEmpty();
    }
}