package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.DataNotFoundException;
import faang.school.urlshortenerservice.properties.CleanerProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlServiceImplTest {

    @InjectMocks
    private UrlServiceImpl urlService;

    @Mock
    private HashRepository hashRepository;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private HashCache hashCache;

    @Mock
    private CleanerProperties cleanerProperties;

    private static final String TEST_HASH = "abc123";
    private static final String TEST_URL = "https://www.google.com";

    @Test
    void cleanOldUrls_ShouldDeleteOldUrlsAndSaveHashes() {
        List<String> deletedHashes = List.of("hash1", "hash2", "hash3");
        when(urlRepository.deleteOldUrlsAndReturnHashes(any(LocalDateTime.class)))
                .thenReturn(deletedHashes);

        urlService.cleanOldUrls();

        verify(urlRepository).deleteOldUrlsAndReturnHashes(any(LocalDateTime.class));

        verify(hashRepository).save(deletedHashes);

        verify(urlRepository, times(1)).deleteOldUrlsAndReturnHashes(any(LocalDateTime.class));
        verify(hashRepository, times(1)).save(deletedHashes);
    }

    @Test
    void cleanOldUrls_ShouldNotSaveHashesWhenNoOldUrls() {
        when(urlRepository.deleteOldUrlsAndReturnHashes(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        urlService.cleanOldUrls();

        verify(urlRepository).deleteOldUrlsAndReturnHashes(any(LocalDateTime.class));

        verify(hashRepository, never()).save(anyList());
    }

    @Test
    void getUrl_ShouldReturnUrlFromRedisCache() {
        when(urlCacheRepository.getUrlByHash(TEST_HASH))
                .thenReturn(Optional.of(TEST_URL));

        UrlDto result = urlService.getUrl(TEST_HASH);

        assertNotNull(result);
        assertEquals(TEST_URL, result.url());

        verify(urlCacheRepository).getUrlByHash(TEST_HASH);

        verify(urlRepository, never()).findUrlByHash(anyString());
    }

    @Test
    void getUrl_ShouldReturnUrlFromDatabaseWhenNotInCache() {
        when(urlCacheRepository.getUrlByHash(TEST_HASH))
                .thenReturn(Optional.empty());
        when(urlRepository.findUrlByHash(TEST_HASH))
                .thenReturn(Optional.of(TEST_URL));

        UrlDto result = urlService.getUrl(TEST_HASH);

        assertNotNull(result);
        assertEquals(TEST_URL, result.url());

        verify(urlCacheRepository).getUrlByHash(TEST_HASH);

        verify(urlRepository).findUrlByHash(TEST_HASH);

        verify(urlCacheRepository).save(TEST_HASH, TEST_URL);
    }

    @Test
    void getUrl_ShouldThrowExceptionWhenUrlNotFound() {
        when(urlCacheRepository.getUrlByHash(TEST_HASH))
                .thenReturn(Optional.empty());
        when(urlRepository.findUrlByHash(TEST_HASH))
                .thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> urlService.getUrl(TEST_HASH)
        );

        assertEquals("URL not found for hash: " + TEST_HASH, exception.getMessage());

        verify(urlCacheRepository).getUrlByHash(TEST_HASH);
        verify(urlRepository).findUrlByHash(TEST_HASH);

        verify(urlCacheRepository, never()).save(anyString(), anyString());
    }

    @Test
    void createShortUrl_ShouldCreateShortUrl() {
        UrlDto urlDto = new UrlDto(TEST_URL);
        when(hashCache.getHash()).thenReturn(TEST_HASH);

        String result = urlService.createShortUrl(urlDto);

        assertNotNull(result);
        assertEquals(TEST_HASH, result);

        verify(hashCache).getHash();

        verify(urlRepository).save(TEST_HASH, TEST_URL);

        verify(urlCacheRepository).save(TEST_HASH, TEST_URL);
    }

    @Test
    void createShortUrl_ShouldCallComponentsInCorrectOrder() {
        UrlDto urlDto = new UrlDto(TEST_URL);
        when(hashCache.getHash()).thenReturn(TEST_HASH);

        urlService.createShortUrl(urlDto);

        var inOrder = inOrder(hashCache, urlRepository, urlCacheRepository);

        inOrder.verify(hashCache).getHash();
        inOrder.verify(urlRepository).save(TEST_HASH, TEST_URL);
        inOrder.verify(urlCacheRepository).save(TEST_HASH, TEST_URL);
    }

    @Test
    void createShortUrl_ShouldCreateDifferentHashesForSameUrl() {
        UrlDto urlDto = new UrlDto(TEST_URL);
        when(hashCache.getHash())
                .thenReturn("hash1")
                .thenReturn("hash2");

        String result1 = urlService.createShortUrl(urlDto);
        String result2 = urlService.createShortUrl(urlDto);

        assertNotEquals(result1, result2);
        assertEquals("hash1", result1);
        assertEquals("hash2", result2);

        verify(hashCache, times(2)).getHash();
    }
}
