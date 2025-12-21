package faang.school.urlshortenerservice.integration.service.url;

import faang.school.urlshortenerservice.integration.AbstractIntegrationTest;
import faang.school.urlshortenerservice.config.UrlCacheProperties;
import faang.school.urlshortenerservice.repository.redis.UrlCacheRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Redis URL cache TTL behavior")
public class UrlCacheRepositoryTtlTest extends AbstractIntegrationTest {
    @Autowired
    UrlCacheRepositoryImpl repository;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private UrlCacheProperties urlCacheProperties;

    @Test
    @DisplayName("Stores URL with TTL and removes key from Redis after TTL expiration")
    void save_setsTtl_andKeyExpires() throws Exception {
        String hash = "ABC123";
        String longUrl = "https://example.com";

        repository.save(hash, longUrl);

        String key = urlCacheProperties.getModule()
                + ":"
                + urlCacheProperties.getVersion()
                + ":"
                + urlCacheProperties.getUrlEntity()
                + ":"
                + hash;

        assertThat(redisTemplate.opsForValue().get(key))
                .isEqualTo(longUrl);

        Long ttlSeconds = redisTemplate.getExpire(key);
        assertThat(ttlSeconds).isGreaterThan(0);

        Thread.sleep(Duration.ofSeconds(3).toMillis());

        assertThat(redisTemplate.opsForValue().get(key)).isNull();
    }
}
