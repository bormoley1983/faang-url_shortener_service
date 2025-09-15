package faang.school.urlshortenerservice.service;

import com.redis.testcontainers.RedisContainer;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@Sql(scripts = "/db/script/url_insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/db/script/url_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class UrlServiceIntTest {
    @Autowired
    private UrlService service;
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private HashRepository hashRepository;

    @Container
    public static PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:13.3");
    @Container
    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis/redis-stack:latest"));

    private static final String LONG_URL = "https://somesource.ru/super/long/url";
    private static final String EXISTS_URL = "https://www.baeldung.com";
    private static final String HASH = "999999";
    private static final String REMOVED_HASH = "ZZZZZZ";
    private static final String UNKNOWN_HASH = "AAAAAA";

    @DynamicPropertySource
    static void propertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Успешное создание короткой ссылки")
    void positive_shouldCreateShortUrl() {
        UrlDto dto = new UrlDto(LONG_URL, null);

        UrlDto shortUrl = service.getShortUrl(dto);

        assertTrue(urlRepository.findById(shortUrl.hash()).isPresent());
    }

    @Test
    @DisplayName("Успешное получение и обновление актуальности оригинальной ссылки")
    void positive_shouldReturnOriginalUrlAndUpdateLastRequestDate() {
        Url expected = urlRepository.findById(HASH).get();

        String actualUrl = service.getOriginalUrl(HASH);

        assertEquals(EXISTS_URL, actualUrl);
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    Url actual = urlRepository.findById(HASH).get();
                    assertNotEquals(expected.getLastRequestedAt(), actual.getLastRequestedAt());
                    assertTrue(expected.getLastRequestedAt().isBefore(actual.getLastRequestedAt()));

                });
    }

    @Test
    @DisplayName("Успешное удаление устаревших ссылок и повторное сохранение хэшей")
    void positive_shouldCleanExpiredUrlAndReleaseHashes() {
        service.cleanExpiredUrlAndReleaseHashes();

        assertFalse(urlRepository.findById(REMOVED_HASH).isPresent());
        assertTrue(hashRepository.findById(REMOVED_HASH).isPresent());
    }

    @Test
    @DisplayName("Успешное удаление устаревших ссылок и повторное сохранение хэшей")
    void negative_whenOriginalUrlNotFound_throwsError() {
        String expectedMessage = "Hash %s not found".formatted(UNKNOWN_HASH);

        String actualMessage = assertThrows(EntityNotFoundException.class,
                () -> service.getOriginalUrl(UNKNOWN_HASH)).getMessage();

        assertEquals(expectedMessage, actualMessage);
    }
}