package faang.school.urlshortenerservice.integration.service.url;

import faang.school.urlshortenerservice.config.UrlCacheProperties;
import faang.school.urlshortenerservice.config.UrlServiceProperties;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.integration.base.AbstractIntegrationTest;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.service.HashCache;
import faang.school.urlshortenerservice.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("UrlService Integration Test: service logic integrated with DB and Redis")
class UrlServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    UrlService urlService;

    @Autowired
    UrlRepository urlRepository;

    @Autowired
    StringRedisTemplate redis;

    @Autowired
    UrlCacheProperties urlCacheProperties;

    @Autowired
    UrlServiceProperties urlServiceProperties;

    // We control hashes for createShortUrl, but still use real DB + Redis
    @MockBean
    HashCache hashCache;

    @BeforeEach
    void setUp() {
        urlRepository.deleteAll();
        flushRedis();
    }

    @Test
    @DisplayName("createShortUrl: saves to DB, saves to Redis, returns baseUrl/hash")
    void createShortUrl_persistsToDb_andCachesToRedis() {
        when(hashCache.getHash()).thenReturn("ABC123");

        String longUrl = "https://example.com/some/long/url";

        String shortUrl = urlService.createShortUrl(longUrl);

        assertThat(shortUrl).isEqualTo(urlServiceProperties.getBaseUrl() + "/ABC123");

        // DB
        assertThat(urlRepository.findById("ABC123")).isPresent();
        assertThat(urlRepository.findById("ABC123").get().getUrl()).isEqualTo(longUrl);

        // Redis
        assertThat(redis.opsForValue().get(buildKey("ABC123"))).isEqualTo(longUrl);
    }

    @Test
    @DisplayName("getOriginalUrl: cache HIT returns from Redis (DB may be empty)")
    void getOriginalUrl_cacheHit_returnsFromRedis() {
        String hash = "HIT001";
        String longUrl = "https://example.com/hit";

        // Put to cache only, no DB insert
        redis.opsForValue().set(buildKey(hash), longUrl);

        String resolved = urlService.getOriginalUrl(hash);

        assertThat(resolved).isEqualTo(longUrl);
        assertThat(urlRepository.findById(hash)).isNotPresent();
    }

    @Test
    @DisplayName("getOriginalUrl: cache MISS loads from DB and warms Redis")
    void getOriginalUrl_cacheMiss_loadsFromDb_andCaches() {
        String hash = "MISS01";
        String longUrl = "https://example.com/miss";

        urlRepository.save(new UrlEntity(hash, longUrl));
        assertThat(redis.opsForValue().get(buildKey(hash))).isNull();

        String resolved = urlService.getOriginalUrl(hash);

        assertThat(resolved).isEqualTo(longUrl);
        assertThat(redis.opsForValue().get(buildKey(hash))).isEqualTo(longUrl);
    }

    @Test
    @DisplayName("getOriginalUrl: null or blank hash throws IllegalArgumentException")
    void getOriginalUrl_invalidHash_throwsIllegalArgument() {
        assertThatThrownBy(() -> urlService.getOriginalUrl(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> urlService.getOriginalUrl(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private String buildKey(String hash) {
        return urlCacheProperties.getModule()
                + ":" + urlCacheProperties.getVersion()
                + ":" + urlCacheProperties.getUrlEntity()
                + ":" + hash;
    }

    private void flushRedis() {
        var cf = redis.getConnectionFactory();
        if (cf != null) {
            try (var conn = cf.getConnection()) {
                conn.serverCommands();
            }
        }
    }
}
