package faang.school.urlshortenerservice.integration.service.cleaner;

import faang.school.urlshortenerservice.integration.AbstractIntegrationTest;
import faang.school.urlshortenerservice.repository.db.HashRepository;
import faang.school.urlshortenerservice.service.CleanerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

class CleanerRollbackIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CleanerService cleanerService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private HashRepository hashRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE url");
        jdbcTemplate.execute("TRUNCATE TABLE hash");
        jdbcTemplate.execute("ALTER SEQUENCE unique_number_seq RESTART WITH 1");
    }

    @Test
    @DisplayName("clean(): при ошибке сохранения хэшей транзакция откатывается (url не удаляются)")
    void clean_rollsBack_whenHashSaveFails() {
        OffsetDateTime oldCreatedAt = OffsetDateTime.now(ZoneOffset.UTC).minusDays(370);
        jdbcTemplate.update(
                "INSERT INTO url(hash, url, created_at) VALUES (?, ?, ?)",
                "cccccc", "https://old.example", oldCreatedAt
        );

        doThrow(new RuntimeException("db down")).when(hashRepository).save(anyList());

        assertThatThrownBy(() -> cleanerService.clean())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db down");

        // delete откатился
        assertThat(existsUrlHash("cccccc")).isTrue();
        assertThat(existsHash("cccccc")).isFalse();
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