package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.exception.HashUnavailableException;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.repository.redis.UrlCacheRepository;
import faang.school.urlshortenerservice.config.UrlServiceProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

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

@DisplayName("UrlService createShortUrl")
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

        // 1-я попытка: коллизия в БД
        doThrow(new DataIntegrityViolationException("dup"))
                .doAnswer(inv -> inv.getArgument(0))
                .when(urlRepository).save(any(UrlEntity.class));

        String shortUrl = service.createShortUrl(longUrl);

        assertThat(shortUrl).isEqualTo("http://short/BBBBBB");

        // DB save должен быть вызван 2 раза (2 попытки)
        verify(urlRepository, times(2)).save(any(UrlEntity.class));

        // Redis должен быть вызван 1 раз и только с финальным hash
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
}