package faang.school.urlshortenerservice.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlCacheRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UrlCacheRepository urlCacheRepository;

    @Test
    void test_getCachedUrl_whenKeyExists_shouldReturnUrl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:abc123")).thenReturn("https://example.com");

        Optional<String> result = urlCacheRepository.getCachedUrl("abc123");

        assertThat(result).contains("https://example.com");
        verify(valueOperations).get("url:abc123");
    }

    @Test
    void test_getCachedUrl_whenKeyNotExists_shouldReturnEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:abc123")).thenReturn(null);

        Optional<String> result = urlCacheRepository.getCachedUrl("abc123");

        assertThat(result).isEmpty();
        verify(valueOperations).get("url:abc123");
    }

    @Test
    void test_getCachedUrl_whenRedisThrowsException_shouldReturnEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("url:abc123")).thenThrow(new RuntimeException("Redis error"));

        Optional<String> result = urlCacheRepository.getCachedUrl("abc123");

        assertThat(result).isEmpty();
        verify(valueOperations).get("url:abc123");
    }
}