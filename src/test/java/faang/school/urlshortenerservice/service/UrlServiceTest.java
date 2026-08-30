package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.exception.InvalidUrlException;
import faang.school.urlshortenerservice.exception.UrlExpiredException;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.net.InetAddress;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlCacheRepository urlCacheRepository;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private HashCache hashCache;

    @Mock
    private HostAddressResolver hostAddressResolver;

    private UrlService urlService;

    @BeforeEach
    void setUp() throws Exception {
        urlService = new UrlService(urlCacheRepository, urlRepository, hashCache, hostAddressResolver);
        ReflectionTestUtils.setField(urlService, "numberOfDaysForOutdatedHashes", 365);
        ReflectionTestUtils.setField(urlService, "domain", "http://localhost:18080/url");
        ReflectionTestUtils.setField(urlService, "allowPrivateNetworkTargets", false);
        org.mockito.Mockito.lenient()
                .when(hostAddressResolver.resolve(anyString()))
                .thenReturn(new InetAddress[]{InetAddress.getByName("203.0.113.1")});
    }

    @Test
    void generateShortUrl_shouldReturnSavedUrl_whenHashPresent() {
        String inputUrl = "http://example.com";
        String generatedHash = "abc123";

        when(hashCache.getNextHash()).thenReturn(generatedHash);

        Url savedUrl = Url.builder()
                .url(inputUrl)
                .hash(generatedHash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(365))
                .build();
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        String result = urlService.generateShortUrl(inputUrl);

        assertTrue(StringUtils.hasText(result));
        assertTrue(result.endsWith("/" + generatedHash));
        verify(urlRepository).save(any(Url.class));
        verify(urlCacheRepository).saveUrl(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void generateShortUrl_shouldThrowException_whenHashNotPresent() {
        String inputUrl = "http://example.com";

        when(hashCache.getNextHash()).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> urlService.generateShortUrl(inputUrl));
        assertEquals("Failed to generate hash for URL", exception.getMessage());
    }

    @Test
    void getUrl_shouldReturnFromCache_whenResultPresent() {
        String hash = "hash1";
        String cachedUrl = "http://cached.com";

        when(urlCacheRepository.getUrl(hash)).thenReturn(cachedUrl);

        String result = urlService.getUrl(hash);

        assertEquals(cachedUrl, result);
        verify(urlCacheRepository, never()).saveUrl(any(), any(), any());
    }

    @Test
    void getUrl_shouldQueryRepository_whenNotInCache() {
        String hash = "hash1";
        String repoUrl = "http://repository.com";

        when(urlCacheRepository.getUrl(hash)).thenReturn("");

        Url url = Url.builder()
                .hash(hash)
                .url(repoUrl)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(365))
                .build();
        when(urlRepository.findByHash(hash)).thenReturn(Optional.of(url));

        String result = urlService.getUrl(hash);

        assertEquals(repoUrl, result);
        verify(urlCacheRepository).saveUrl(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void getUrl_shouldThrowUrlNotFoundException_whenNotFoundInCacheAndRepo() {
        String hash = "hash1";

        when(urlCacheRepository.getUrl(hash)).thenReturn("");
        when(urlRepository.findByHash(hash)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> urlService.getUrl(hash));
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenSchemeNotAllowed() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("ftp://example.com/file"));

        assertEquals("Only http and https URLs are allowed", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldRejectHostnameResolvingToPrivateAddress() throws Exception {
        when(hostAddressResolver.resolve("internal.example.com"))
                .thenReturn(new InetAddress[]{InetAddress.getByName("127.0.0.1")});

        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("https://internal.example.com/resource"));

        assertEquals("Private network addresses are not allowed", exception.getMessage());
        verify(hashCache, never()).getNextHash();
    }

    @Test
    void generateShortUrl_shouldRejectUniqueLocalIpv6Address() throws Exception {
        when(hostAddressResolver.resolve("[fc00::1]"))
                .thenReturn(new InetAddress[]{InetAddress.getByName("fc00::1")});

        assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://[fc00::1]/resource"));
        verify(hashCache, never()).getNextHash();
    }

    @Test
    void getUrl_shouldThrowUrlExpiredException_whenStoredUrlExpired() {
        String hash = "hash1";

        when(urlCacheRepository.getUrl(hash)).thenReturn("");
        when(urlRepository.findByHash(hash)).thenReturn(Optional.of(Url.builder()
                .hash(hash)
                .url("https://example.com")
                .createdAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build()));

        assertThrows(UrlExpiredException.class, () -> urlService.getUrl(hash));
        verify(urlCacheRepository).deleteByHash(hash);
    }
}
