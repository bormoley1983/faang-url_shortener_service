package faang.school.urlshortenerservice.integration.repository;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.integration.base.AbstractIntegrationTest;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UrlRepository Integration Test: url table operations only")
class UrlRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE url");
    }

    @Test
    @DisplayName("save + findById: persists url mapping")
    void save_and_findById() {
        urlRepository.save(new UrlEntity("ABC123", "https://example.com/a"));

        assertThat(urlRepository.findById("ABC123")).isPresent();
        assertThat(urlRepository.findById("ABC123").get().getUrl()).isEqualTo("https://example.com/a");
    }

    @Test
    @DisplayName("deleteExpiredReturningHashes(threshold): deletes only expired rows and returns their hashes")
    void deleteExpiredReturningHashes_deletesExpiredOnly() {
        // Insert directly to control created_at precisely
        OffsetDateTime oldTs = OffsetDateTime.now().minusDays(10);
        OffsetDateTime newTs = OffsetDateTime.now().minusHours(1);

        jdbcTemplate.update(
                "INSERT INTO url(hash, url, created_at) VALUES (?, ?, ?)",
                "OLD111", "https://example.com/old", oldTs
        );
        jdbcTemplate.update(
                "INSERT INTO url(hash, url, created_at) VALUES (?, ?, ?)",
                "NEW222", "https://example.com/new", newTs
        );

        OffsetDateTime threshold = OffsetDateTime.now().minusDays(5);

        List<String> freed = urlRepository.deleteExpiredReturningHashes(threshold);

        assertThat(freed).containsExactly("OLD111");

        // OLD is deleted
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM url WHERE hash = 'OLD111'", Integer.class)).isZero();

        // NEW remains
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM url WHERE hash = 'NEW222'", Integer.class)).isEqualTo(1);
    }
}
