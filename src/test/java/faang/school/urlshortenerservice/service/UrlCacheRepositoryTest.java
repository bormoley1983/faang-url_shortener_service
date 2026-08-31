package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlCacheRepositoryTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UrlCacheRepository urlCacheRepository;

    @BeforeEach
    void setUp() {
        urlCacheRepository = new UrlCacheRepository(redisTemplate);
        ReflectionTestUtils.setField(urlCacheRepository, "keyPrefix", "url:redirect:v1:");
        // lenient: not every test path touches opsForValue (e.g. deleteByHash/containsUrl)
        org.mockito.Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveUrl_shouldWriteWithPrefixedKeyAndTtl() {
        urlCacheRepository.saveUrl("abc123", "https://example.com", Duration.ofHours(1));

        verify(valueOperations).set(eq("url:redirect:v1:abc123"), eq("https://example.com"), eq(Duration.ofHours(1)));
    }

    @Test
    void getUrl_shouldReadWithPrefixedKey() {
        when(valueOperations.get("url:redirect:v1:abc123")).thenReturn("https://example.com");

        String result = urlCacheRepository.getUrl("abc123");

        assertEquals("https://example.com", result);
    }

    @Test
    void getUrl_shouldReturnNull_whenKeyAbsent() {
        when(valueOperations.get(anyString())).thenReturn(null);

        assertNull(urlCacheRepository.getUrl("missing"));
    }

    @Test
    void containsUrl_shouldReflectRedisHasKey() {
        when(redisTemplate.hasKey("url:redirect:v1:abc123")).thenReturn(true);
        assertTrue(urlCacheRepository.containsUrl("abc123"));

        when(redisTemplate.hasKey("url:redirect:v1:missing")).thenReturn(false);
        assertFalse(urlCacheRepository.containsUrl("missing"));
    }

    @Test
    void deleteByHash_shouldDeletePrefixedKey() {
        urlCacheRepository.deleteByHash("abc123");

        verify(redisTemplate).delete("url:redirect:v1:abc123");
    }

    @Test
    void saveUrl_shouldUseConfiguredPrefix() {
        ReflectionTestUtils.setField(urlCacheRepository, "keyPrefix", "custom:prefix:");

        urlCacheRepository.saveUrl("h1", "https://example.com", Duration.ofMinutes(5));

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(key.capture(), eq("https://example.com"), any(Duration.class));
        assertEquals("custom:prefix:h1", key.getValue());
    }
}
