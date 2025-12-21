package faang.school.urlshortenerservice;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.NoFreeHashesException;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.service.UrlService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private HashCache hashCache;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private HashRepository hashRepository;

    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "expirationDays", 1L);
    }

    @Test
    void createShortUrl_shouldSaveUrlAndCacheIt() {
        String hash = "abc123";
        String longUrl = "https://example.com";

        when(hashCache.getHash()).thenReturn(hash);

        String result = urlService.createShortUrl(longUrl);

        Assertions.assertEquals(hash, result);

        verify(hashCache).getHash();
        verify(urlRepository).save(argThat(url ->
                url.getHash().equals(hash) &&
                        url.getUrl().equals(longUrl)
        ));
        verify(urlCacheRepository, atLeastOnce()).save(hash, longUrl);
    }

    @Test
    void getShortUrl_shouldReturnFromCache_whenPresent() {
        String hash = "abc123";
        String longUrl = "https://cached.com";

        when(urlCacheRepository.get(hash)).thenReturn(longUrl);

        String result = urlService.getShortUrl(hash);

        Assertions.assertEquals(longUrl, result);
        verify(urlRepository, never()).findById(any());
    }

    @Test
    void getShortUrl_shouldLoadFromDbAndCache_whenNotInCache() {
        String hash = "abc123";
        String longUrl = "https://db.com";

        Url url = Url.builder()
                .hash(hash)
                .url(longUrl)
                .build();

        when(urlCacheRepository.get(hash)).thenReturn(null);
        when(urlRepository.findById(hash)).thenReturn(Optional.of(url));

        String result = urlService.getShortUrl(hash);

        Assertions.assertEquals(longUrl, result);
        verify(urlCacheRepository).save(hash, longUrl);
    }

    @Test
    void getShortUrl_shouldThrowException_whenNotFound() {
        String hash = "missing";

        when(urlCacheRepository.get(hash)).thenReturn(null);
        when(urlRepository.findById(hash)).thenReturn(Optional.empty());

        assertThrows(NoFreeHashesException.class,
                () -> urlService.getShortUrl(hash));
    }

    @Test
    void cleanOldUrls_shouldDoNothing_whenNoExpiredUrls() {
        when(urlRepository.deleteOldUrlsAndReturnHashes(any()))
                .thenReturn(List.of());

        urlService.cleanOldUrls();

        verify(hashRepository, never()).returnHashes(any());
    }

    @Test
    void cleanOldUrls_shouldReturnHashes_whenExpiredUrlsExist() {
        List<String> freedHashes = List.of("a", "b", "c");

        when(urlRepository.deleteOldUrlsAndReturnHashes(any()))
                .thenReturn(freedHashes);

        urlService.cleanOldUrls();

        verify(hashRepository)
                .returnHashes(freedHashes.toArray(new String[0]));
    }
}
