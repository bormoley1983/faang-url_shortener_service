package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.property.HashProps;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
class HashGeneratorIntTest {
    @Autowired
    private HashGenerator hashGenerator;
    @Autowired
    private HashRepository hashRepository;
    @Autowired
    private HashProps hashProps;

    @Container
    public static PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:13.3");

    @DynamicPropertySource
    static void propertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Успешная генерация пачки хэшей")
    void positive_shouldGenerateBatch() {
        long expected = hashRepository.count() + hashProps.batchSize();

        hashGenerator.generateBatch();
        long actual = hashRepository.count();

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Успешное получение пачки хэшей")
    void positive_shouldReturnsHashes() {
        List<String> hashes = hashGenerator.getHashes(hashProps.batchSize());

        assertNotNull(hashes);
        assertEquals(hashProps.batchSize(), hashes.size());
    }

    @Test
    @DisplayName("Успешная асинхронная генерация пачки хэшей")
    void positive_shouldGenerateBatchAsync() {
        long expected = hashRepository.count() + hashProps.batchSize();

        hashGenerator.generateBatchAsync();

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    long actual = hashRepository.count();
                    assertEquals(expected, actual);
                });
    }

    @Test
    @DisplayName("Успешное асинхронное получение пачки хэшей")
    void positive_shouldReturnsHashesAsync() {
        List<String> hashes = new ArrayList<>(hashProps.batchSize());
        hashGenerator.getHashesAsync(hashProps.batchSize()).thenAccept(hashes::addAll);

        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertFalse(hashes.isEmpty());
                    assertEquals(hashProps.batchSize(), hashes.size());
                });
    }

    @Test
    @DisplayName("Успешная генерация хэшей - меньше минимума")
    void positive_whenHashesNotEnough_shouldGenerateBatch() {
        hashRepository.deleteAll();

        hashGenerator.generateBatchIfNeeded();
        List<Hash> actual = hashRepository.findAll();

        assertFalse(actual.isEmpty());
    }

    @Test
    @DisplayName("Успешная НЕ генерация хэшей - достаточно")
    void positive_whenHashesEnough_shouldNotGenerateBatch() {
        long expected = hashRepository.count();
        if (expected < hashProps.minStored()) {
            hashGenerator.generateBatch();
            expected += hashProps.batchSize();
        }

        hashGenerator.generateBatchIfNeeded();
        long actual = hashRepository.count();

        assertEquals(expected, actual);
    }
}