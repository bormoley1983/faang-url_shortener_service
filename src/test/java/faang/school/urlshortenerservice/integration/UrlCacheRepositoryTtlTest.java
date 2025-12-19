package faang.school.urlshortenerservice.integration;

import faang.school.urlshortenerservice.AbstractIntegrationTest;
import faang.school.urlshortenerservice.repository.redis.UrlCacheRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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

    @Test
    @DisplayName("""
        Stores URL with TTL and
        removes key from Redis after TTL expiration
        """)
    void save_setsTtl_andKeyExpires() throws Exception {
        String hash = "ABC123";
        String longUrl = "https://example.com";

        repository.save(hash, longUrl);

        String key = "urlshortener:v1:url:" + hash;

        assertThat(redisTemplate.opsForValue().get(key))
                .isEqualTo(longUrl);

        Long ttlSeconds = redisTemplate.getExpire(key);
        assertThat(ttlSeconds).isGreaterThan(0);

        Thread.sleep(Duration.ofSeconds(3).toMillis());

        assertThat(redisTemplate.opsForValue().get(key)).isNull();
    }
}
