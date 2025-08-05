package faang.school.urlshortenerservice.service;


import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class UrlServiceTest {
    private UrlCacheRepository urlCacheRepository;
    private UrlRepository urlRepository;
    private HashCacheService hashCacheService;
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlCacheRepository = mock(UrlCacheRepository.class);
        urlRepository = mock(UrlRepository.class);
        hashCacheService = mock(HashCacheService.class);
        urlService = new UrlService(urlCacheRepository, urlRepository, hashCacheService);
    }

    @Test
    void testShouldReturnExistingShortUrlIfExists() {
        String originalUrl = "https://example.com";
        Url existing = new Url("abc123", originalUrl);
        when(urlRepository.findByUrl(originalUrl)).thenReturn(Optional.of(existing));
        String result = urlService.getShortUrl(originalUrl, "https://short.ly");
        assertThat(result).isEqualTo("https://short.ly/abc123");
        verifyNoInteractions(hashCacheService);
    }

    @Test
    void testShouldGenerateNewShortUrlIfNotExists() {
        String originalUrl = "https://example.com";
        String baseUrl = "https://short.ly";
        when(urlRepository.findByUrl(originalUrl)).thenReturn(Optional.empty());
        when(hashCacheService.getHash()).thenReturn("xyz789");
        String result = urlService.getShortUrl(originalUrl, baseUrl);
        assertThat(result).isEqualTo("https://short.ly/xyz789");
        ArgumentCaptor<Url> urlCaptor = ArgumentCaptor.forClass(Url.class);
        verify(urlRepository).save(urlCaptor.capture());
        assertThat(urlCaptor.getValue().getHash()).isEqualTo("xyz789");
        assertThat(urlCaptor.getValue().getUrl()).isEqualTo(originalUrl);
        verify(urlCacheRepository).cacheLongUrl("xyz789", originalUrl);
    }

    @Test
    void testShouldReturnFromCacheIfExists() {
        when(urlCacheRepository.getLongUrl("abc123")).thenReturn("https://example.com");
        String result = urlService.getLongUrl("abc123");
        assertThat(result).isEqualTo("https://example.com");
        verifyNoInteractions(urlRepository);
    }

    @Test
    void testShouldLoadFromDbIfCacheMiss() {
        String hash = "abc123";
        String longUrl = "https://example.com";
        when(urlCacheRepository.getLongUrl(hash)).thenReturn(null);
        when(urlRepository.findByHash(hash)).thenReturn(Optional.of(new Url(hash, longUrl)));
        String result = urlService.getLongUrl(hash);
        assertThat(result).isEqualTo(longUrl);
        verify(urlCacheRepository).cacheLongUrl(hash, longUrl);
    }

    @Test
    void testShouldThrowIfNotFoundAnywhere() {
        String hash = "abc123";

        when(urlCacheRepository.getLongUrl(hash)).thenReturn(null);
        when(urlRepository.findByHash(hash)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> urlService.getLongUrl(hash))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessage("No URL found for hash: abc123");
    }
}
