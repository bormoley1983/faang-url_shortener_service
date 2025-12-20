package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.exception.HashUnavailableException;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.repository.redis.UrlCacheRepository;
import faang.school.urlshortenerservice.config.UrlServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("UrlService createShortUrl, getOriginalUrl")
class UrlServiceImplTest {

    private HashCache hashCache;
    private UrlRepository urlRepository;
    private UrlCacheRepository urlCacheRepository;
    private UrlServiceProperties props;

    private UrlServiceImpl service;

    @BeforeEach
    void setUp() {
        hashCache = mock(HashCache.class);
        urlRepository = mock(UrlRepository.class);
        urlCacheRepository = mock(UrlCacheRepository.class);
        props = mock(UrlServiceProperties.class);

        when(props.getBaseUrl()).thenReturn("http://short");
        service = new UrlServiceImpl(hashCache, urlRepository, urlCacheRepository, props);
    }

    @Test
    void createShortUrl_firstSaveThrows_thenSecondSucceeds_redisCalledOnce() {
        String longUrl = "https://example.com/abc";
        when(hashCache.getHash()).thenReturn("AAAAAA", "BBBBBB");

        doThrow(new DataIntegrityViolationException("dup"))
                .doAnswer(inv -> inv.getArgument(0))
                .when(urlRepository).save(any(UrlEntity.class));

        String shortUrl = service.createShortUrl(longUrl);

        assertThat(shortUrl).isEqualTo("http://short/BBBBBB");

        verify(urlRepository, times(2)).save(any(UrlEntity.class));

        verify(urlCacheRepository, times(1)).save("BBBBBB", longUrl);
        verify(urlCacheRepository, never()).save("AAAAAA", longUrl);
    }

    @Test
    void createShortUrl_happyPath_callsDbAndRedis_andReturnsShortUrl() {
        String longUrl = "https://example.com";
        when(hashCache.getHash()).thenReturn("ABC123");

        String shortUrl = service.createShortUrl(longUrl);

        assertThat(shortUrl).isEqualTo("http://short/ABC123");

        ArgumentCaptor<UrlEntity> captor = ArgumentCaptor.forClass(UrlEntity.class);
        verify(urlRepository).save(captor.capture());
        assertThat(captor.getValue().getHash()).isEqualTo("ABC123");
        assertThat(captor.getValue().getUrl()).isEqualTo(longUrl);

        verify(urlCacheRepository).save("ABC123", longUrl);
    }

    @Test
    void createShortUrl_hashUnavailable_throws() {
        when(hashCache.getHash()).thenReturn(null, " ", null);

        assertThatThrownBy(() -> service.createShortUrl("https://example.com"))
                .isInstanceOf(HashUnavailableException.class);

        verifyNoInteractions(urlRepository);
        verifyNoInteractions(urlCacheRepository);
    }

    @Test
    @DisplayName("Throws HashUnavailableException after max attempts exhausted")
    void createShortUrl_maxAttemptsExceeded_throws() {
        when(hashCache.getHash()).thenReturn(null, " ", null);

        assertThatThrownBy(() -> service.createShortUrl("https://example.com"))
                .isInstanceOf(HashUnavailableException.class);

        verifyNoInteractions(urlRepository);
        verifyNoInteractions(urlCacheRepository);
    }

    @Test
    @DisplayName("getOriginalUrl throws UrlNotFoundException for null or blank hash")
    void getOriginalUrl_invalidHash_throws() {
        assertThatThrownBy(() -> service.getOriginalUrl(null))
                .isInstanceOf(UrlNotFoundException.class);

        assertThatThrownBy(() -> service.getOriginalUrl(" "))
                .isInstanceOf(UrlNotFoundException.class);

        verifyNoInteractions(urlRepository);
        verifyNoInteractions(urlCacheRepository);
    }

    @Test
    @DisplayName("getOriginalUrl returns value from cache when cache hit")
    void getOriginalUrl_cacheHit_returnsCached() {
        when(urlCacheRepository.find("ABC123"))
                .thenReturn(Optional.of("https://example.com"));

        String result = service.getOriginalUrl("ABC123");

        assertThat(result).isEqualTo("https://example.com");

        verify(urlCacheRepository).find("ABC123");
        verifyNoInteractions(urlRepository);
    }

    @Test
    @DisplayName("getOriginalUrl loads from DB on cache miss and saves to cache")
    void getOriginalUrl_cacheMiss_dbHit_savesToCache() {
        UrlEntity entity = new UrlEntity("ABC123", "https://example.com");

        when(urlCacheRepository.find("ABC123"))
                .thenReturn(Optional.empty());
        when(urlRepository.findById("ABC123"))
                .thenReturn(Optional.of(entity));

        String result = service.getOriginalUrl("ABC123");

        assertThat(result).isEqualTo("https://example.com");

        verify(urlCacheRepository).find("ABC123");
        verify(urlRepository).findById("ABC123");
        verify(urlCacheRepository).save("ABC123", "https://example.com");
    }

    @Test
    @DisplayName("getOriginalUrl throws when not found in cache and DB")
    void getOriginalUrl_notFoundInCacheAndDb_throws() {
        when(urlCacheRepository.find("ABC123"))
                .thenReturn(Optional.empty());
        when(urlRepository.findById("ABC123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOriginalUrl("ABC123"))
                .isInstanceOf(UrlNotFoundException.class);

        verify(urlRepository).findById("ABC123");
    }

    @Test
    @DisplayName("getOriginalUrl ignores Redis exception on read and falls back to DB")
    void getOriginalUrl_redisReadFails_fallsBackToDb() {
        UrlEntity entity = new UrlEntity("ABC123", "https://example.com");

        when(urlCacheRepository.find("ABC123"))
                .thenThrow(new RedisSystemException("boom", null));
        when(urlRepository.findById("ABC123"))
                .thenReturn(Optional.of(entity));

        String result = service.getOriginalUrl("ABC123");

        assertThat(result).isEqualTo("https://example.com");

        verify(urlRepository).findById("ABC123");
    }

    @Test
    @DisplayName("getOriginalUrl ignores Redis exception on write")
    void getOriginalUrl_redisWriteFails_doesNotFailRequest() {
        UrlEntity entity = new UrlEntity("ABC123", "https://example.com");

        when(urlCacheRepository.find("ABC123"))
                .thenReturn(Optional.empty());
        when(urlRepository.findById("ABC123"))
                .thenReturn(Optional.of(entity));
        doThrow(new RedisConnectionFailureException("down"))
                .when(urlCacheRepository).save(any(), any());

        String result = service.getOriginalUrl("ABC123");

        assertThat(result).isEqualTo("https://example.com");
    }
}