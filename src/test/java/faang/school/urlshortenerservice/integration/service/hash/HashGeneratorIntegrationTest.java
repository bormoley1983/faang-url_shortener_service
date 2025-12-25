package faang.school.urlshortenerservice.integration.service.hash;

import faang.school.urlshortenerservice.integration.base.AbstractIntegrationTest;
import faang.school.urlshortenerservice.repository.db.HashRepository;
import faang.school.urlshortenerservice.service.HashGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HashRepository Integration Test: sequence + batch insert + delete returning")
class HashGeneratorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    HashRepository hashRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    HashGenerator hashGenerator;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE url");
        jdbcTemplate.execute("TRUNCATE TABLE hash");
        jdbcTemplate.execute("ALTER SEQUENCE unique_number_seq RESTART WITH 1");
    }

    @Test
    @DisplayName("generateBatch(): returns number of generated hashes, same as in we save to DB")
    void generateBatch_shouldInsertHashes() {
        CompletableFuture<Integer> future = hashGenerator.generateBatch();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .until(future::isDone);

        Integer generated = future.join();
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM hash", Integer.class);

        assertThat(count).isEqualTo(generated);
    }
}