package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.LocalCache;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {
    @Mock
    private LocalCache localCache;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private HashGenerator hashGenerator;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private HashRepository hashRepository;

    @InjectMocks
    private UrlServiceImpl urlService;

    {
        System.setProperty("domain.prefix", "http://short.est/");
    }

    @Test
    void getUrl_fromCache() {
        when(urlCacheRepository.getCachedUrl("xyz789")).thenReturn(Optional.of("https://google.com"));

        String original = urlService.getUrl("xyz789");

        assertEquals("https://google.com", original);
        verify(urlCacheRepository).getCachedUrl("xyz789");
        verify(urlRepository, never()).findUrlByHash(anyString());
    }

    @Test
    void getUrl_fromDb() {
        when(urlCacheRepository.getCachedUrl("qwe456")).thenReturn(Optional.empty());
        when(urlRepository.findUrlByHash("qwe456")).thenReturn(Optional.of("https://github.com"));

        String original = urlService.getUrl("qwe456");

        assertEquals("https://github.com", original);
        verify(urlRepository).findUrlByHash("qwe456");
    }

    @Test
    void getUrl_notFound() {
        when(urlCacheRepository.getCachedUrl("deadbeef")).thenReturn(Optional.empty());
        when(urlRepository.findUrlByHash("deadbeef")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getUrl("deadbeef"));
    }

    @Test
    void getUrl_emptyHash() {
        assertThrows(UrlNotFoundException.class, () -> urlService.getUrl("   "));
        assertThrows(UrlNotFoundException.class, () -> urlService.getUrl(""));
    }

    @Test
    void cleanHash() {
        when(urlRepository.cleanUnusedHash()).thenReturn(List.of("old1", "old2", "old3"));

        urlService.cleanHash();

        verify(hashGenerator).saveHashByBatch(List.of("old1", "old2", "old3"));
    }

    @Test
    void countHashRepository() {
        when(hashRepository.count()).thenReturn(9999999L);

        long count = urlService.countHashRepository();

        assertEquals(9999999L, count);
    }
}