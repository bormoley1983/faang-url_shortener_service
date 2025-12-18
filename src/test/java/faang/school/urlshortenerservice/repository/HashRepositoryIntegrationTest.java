package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.AbstractIntegrationTest;
import faang.school.urlshortenerservice.repository.db.HashRepository;
import faang.school.urlshortenerservice.service.HashGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HashRepository Integration Test: sequence + batch insert + delete returning")
class HashRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    HashRepository hashRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    HashGenerator hashGenerator;

    @BeforeEach
    void cleanDb() {
        jdbcTemplate.execute("TRUNCATE TABLE hash");
        jdbcTemplate.execute("ALTER SEQUENCE unique_number_seq RESTART WITH 1");
    }

    @Test
    @DisplayName("getUniqueNumbers(n): returns exactly n unique values from DB sequence")
    void getUniqueNumbers_returnsUniqueValues() {
        List<Long> numbers = hashRepository.getUniqueNumbers(5);

        assertThat(numbers).hasSize(5);
        assertThat(new HashSet<>(numbers)).hasSize(5);
        assertThat(numbers).containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    @DisplayName("save(hashes): inserts all provided hashes into pool table")
    void save_insertsAllHashes() {
        hashRepository.save(List.of("a1b2c3", "d4e5f6", "zzzzzz"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hash", Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("save(hashes): duplicates do not fail and are not inserted (ON CONFLICT DO NOTHING)")
    void save_withDuplicates_doesNotFailAndDoesNotInsertDuplicates() {
        hashRepository.save(List.of("aaaaaa", "bbbbbb", "aaaaaa", "cccccc", "bbbbbb"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hash", Integer.class);
        assertThat(count).isEqualTo(3);
        List<String> stored = jdbcTemplate.queryForList("SELECT hash FROM hash ORDER BY hash", String.class);
        assertThat(stored).containsExactly("aaaaaa", "bbbbbb", "cccccc");
    }

    @Test
    @DisplayName("getHashBatch(): returns batchSize hashes and deletes them from pool (no overlap across calls)")
    void getHashBatch_returnsAndDeletesRandomBatch() {
        List<String> hashes = IntStream.range(0, 100)
                .mapToObj(i -> String.format("%06d", i))
                .collect(Collectors.toList());
        hashRepository.save(hashes);

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hash", Integer.class)).isEqualTo(100);

        List<String> batch1 = hashRepository.getHashBatch(10);
        assertThat(batch1).hasSize(10);
        assertThat(new HashSet<>(batch1)).hasSize(10);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hash", Integer.class)).isEqualTo(90);

        List<String> batch2 = hashRepository.getHashBatch(10);
        assertThat(batch2).hasSize(10);
        assertThat(new HashSet<>(batch2)).hasSize(10);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM hash", Integer.class)).isEqualTo(80);

        Set<String> intersection = new HashSet<>(batch1);
        intersection.retainAll(batch2);
        assertThat(intersection).isEmpty();
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