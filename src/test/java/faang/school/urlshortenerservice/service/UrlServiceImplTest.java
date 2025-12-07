package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.CreateUrlDto;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private HashCache hashCache;

    @InjectMocks
    private UrlServiceImpl urlService;

    @Test
    void getOriginalUrl_ShouldReturnFromCache() {
        String hash = "testHash";
        String url = "http://example.com";

        when(urlCacheRepository.get(hash)).thenReturn(url);

        String result = urlService.getOriginalUrl(hash);

        assertEquals(url, result);
        verify(urlCacheRepository).get(hash);
        verifyNoInteractions(urlRepository);
    }

    @Test
    void getOriginalUrl_ShouldLoadFromDatabaseWhenNotInCache() {
        String hash = "testHash";
        String url = "http://example.com";

        when(urlCacheRepository.get(hash)).thenReturn(null);
        when(urlRepository.findUrl(hash)).thenReturn(Optional.of(url));

        String result = urlService.getOriginalUrl(hash);

        assertEquals(url, result);
        verify(urlRepository).findUrl(hash);
        verify(urlCacheRepository).save(eq(hash), eq(url));
    }

    @Test
    void getOriginalUrl_ShouldThrowWhenNotFoundAnywhere() {
        String hash = "missing";

        when(urlCacheRepository.get(hash)).thenReturn(null);
        when(urlRepository.findUrl(hash)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class,
                () -> urlService.getOriginalUrl(hash));
    }

    @Test
    void createShortUrl_ShouldSaveAndCacheAndReturnHash() {
        String url = "http://example.com";
        String hash = "abc123";
        CreateUrlDto dto = new CreateUrlDto(url);

        when(hashCache.getHash()).thenReturn(hash);

        String resultHash = urlService.createShortUrl(dto);

        assertEquals(hash, resultHash);
        verify(hashCache).getHash();
        verify(urlRepository).save(eq(hash), eq(url));
        verify(urlCacheRepository).save(eq(hash), eq(url));
    }
}