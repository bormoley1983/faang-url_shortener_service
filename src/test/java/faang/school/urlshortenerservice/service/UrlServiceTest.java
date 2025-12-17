package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UrlServiceTest {
    @Mock
    private UrlRepository urlRepository;

    @Mock
    private HashCash hashCash;

    @Mock
    private UrlCashRepository urlCacheRepository;

    @InjectMocks
    private UrlService urlService;

    private static final String TEST_HASH = "abc123";
    private static final String TEST_URL = "https://example.com";
    private static final String CACHED_URL = "https://cached-example.com";
    private static final long TTL_DAYS = 100L;
    private static final String PREFIX_URL = "https://sh.c/";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "ttlDays", TTL_DAYS);
        ReflectionTestUtils.setField(urlService, "prefixUrl", PREFIX_URL);
    }

    @Test
    void testGetUrlFromCash() {
        Url cachedUrl = Url.builder().hash(TEST_HASH).url(CACHED_URL).build();
        when(urlCacheRepository.get(TEST_HASH)).thenReturn(cachedUrl);

        String result = urlService.getUrl(TEST_HASH);

        assertEquals(CACHED_URL, result);
        verify(urlCacheRepository).get(TEST_HASH);
        verify(urlRepository, never()).findUrlByHash(any());
    }

    @Test
    void testGetUrlFromRepository() {
        when(urlCacheRepository.get(TEST_HASH)).thenReturn(null);

        Url repositoryUrl = Url.builder().hash(TEST_HASH).url(TEST_URL).build();
        when(urlRepository.findUrlByHash(TEST_HASH))
                .thenReturn(Optional.of(repositoryUrl.getUrl()));

        String result = urlService.getUrl(TEST_HASH);

        assertEquals(TEST_URL, result);
        verify(urlCacheRepository).get(TEST_HASH);
        verify(urlRepository).findUrlByHash(TEST_HASH);
    }

    @Test
    void testGetUrlNotFound() {
        when(urlCacheRepository.get(TEST_HASH)).thenReturn(null);
        when(urlRepository.findUrlByHash(TEST_HASH)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> urlService.getUrl(TEST_HASH)
        );

        assertEquals("Url not found", exception.getMessage());
        verify(urlCacheRepository).get(TEST_HASH);
        verify(urlRepository).findUrlByHash(TEST_HASH);
    }

    @Test
    void testCreateShortUrlAndSave() {
        UrlDto dto = new UrlDto(TEST_URL);
        when(hashCash.getHash()).thenReturn(TEST_HASH);
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = urlService.shortenUrl(dto);

        assertEquals(PREFIX_URL.concat(TEST_HASH), result);

        verify(hashCash).getHash();
        verify(urlRepository).save(argThat(url ->
                url.getHash().equals(TEST_HASH) &&
                        url.getUrl().equals(TEST_URL)
        ));
        verify(urlCacheRepository).save(eq(TEST_HASH), any(Url.class), eq(TTL_DAYS));
    }

    @Test
    void testShortenUrlWithNullDto() {
        assertThrows(NullPointerException.class, () -> urlService.shortenUrl(null));
    }

    @Test
    void testShortenUrlWithEmptyUrl() {
        String emptyUrl = "";
        UrlDto dto = new UrlDto(emptyUrl);
        when(hashCash.getHash()).thenReturn(TEST_HASH);

        String result = urlService.shortenUrl(dto);

        assertEquals(PREFIX_URL.concat(TEST_HASH), result);
        verify(urlRepository).save(argThat(url ->
                url.getUrl().equals(emptyUrl)
        ));
    }
}