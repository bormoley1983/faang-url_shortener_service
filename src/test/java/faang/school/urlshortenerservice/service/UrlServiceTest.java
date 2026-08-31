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

    @Test
    void getUrl_shouldReturnUrl_whenNoExpirySet() {
        String hash = "hash1";
        String repoUrl = "http://no-expiry.com";

        when(urlCacheRepository.getUrl(hash)).thenReturn(null);
        when(urlRepository.findByHash(hash)).thenReturn(Optional.of(Url.builder()
                .hash(hash)
                .url(repoUrl)
                .createdAt(LocalDateTime.now())
                .build()));

        String result = urlService.getUrl(hash);

        assertEquals(repoUrl, result);
        verify(urlCacheRepository, never()).saveUrl(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void getUrl_shouldThrowUrlNotFoundException_whenStoredUrlBlank() {
        String hash = "hash1";

        when(urlCacheRepository.getUrl(hash)).thenReturn("");
        when(urlRepository.findByHash(hash)).thenReturn(Optional.of(Url.builder()
                .hash(hash)
                .url("")
                .createdAt(LocalDateTime.now())
                .build()));

        assertThrows(UrlNotFoundException.class, () -> urlService.getUrl(hash));
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenUrlBlank() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("   "));

        assertEquals("URL must not be blank", exception.getMessage());
        verify(hashCache, never()).getNextHash();
    }

    @Test
    void generateShortUrl_shouldStripSurroundingQuotes_whenQuotedUrl() {
        String inputUrl = "\"http://example.com\"";
        String generatedHash = "abc123";

        when(hashCache.getNextHash()).thenReturn(generatedHash);
        Url savedUrl = Url.builder().url("http://example.com").hash(generatedHash).build();
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        String result = urlService.generateShortUrl(inputUrl);

        assertTrue(result.endsWith("/" + generatedHash));
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenUriMalformed() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://exa mple.com/bad uri"));

        assertEquals("URL is not a valid URI", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowRuntimeException_whenSaveFails() {
        when(hashCache.getNextHash()).thenReturn("abc123");
        when(urlRepository.save(any(Url.class))).thenThrow(new RuntimeException("db down"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> urlService.generateShortUrl("http://example.com"));

        assertEquals("db down", exception.getMessage());
    }

    // NOTE: the "ttl already zero" branch in UrlService.getUrl (skip cache refill when
    // Duration.between(now, expiresAt) <= 0) is timing-dependent and cannot be asserted
    // deterministically without a Clock seam; documented as untestable-without-clock-seam.

    @Test
    void generateShortUrl_shouldRejectMulticastAddress_whenPrivateTargetsDisallowed() throws Exception {
        when(hostAddressResolver.resolve("multicast.example.com"))
                .thenReturn(new InetAddress[]{InetAddress.getByName("224.0.0.1")});

        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://multicast.example.com"));

        assertEquals("Private network addresses are not allowed", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldRejectLinkLocalAddress_whenPrivateTargetsDisallowed() throws Exception {
        when(hostAddressResolver.resolve("linklocal.example.com"))
                .thenReturn(new InetAddress[]{InetAddress.getByName("169.254.0.1")});

        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://linklocal.example.com"));

        assertEquals("Private network addresses are not allowed", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenOpaqueUri() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("mailto:someone@example.com"));

        assertEquals("URL must be hierarchical", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenSchemeMissing() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("example.com/path"));

        assertEquals("URL scheme is required", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenHostMissing() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http:///path-only"));

        assertEquals("URL host is required", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenLocalhost() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://localhost:8080/path"));

        assertEquals("Local hosts are not allowed", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenLocalTld() {
        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://myhost.local/path"));

        assertEquals("Local hosts are not allowed", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenHostUnresolvable() throws Exception {
        when(hostAddressResolver.resolve("missing.example.com"))
                .thenThrow(new java.net.UnknownHostException("missing.example.com"));

        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://missing.example.com/path"));

        assertEquals("URL host cannot be resolved", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldAllowPrivateTarget_whenFlagEnabled() throws Exception {
        // with allowPrivateNetworkTargets=true the SSRF check is skipped entirely,
        // so hostAddressResolver must NOT be consulted for private targets
        ReflectionTestUtils.setField(urlService, "allowPrivateNetworkTargets", true);
        String generatedHash = "abc123";
        when(hashCache.getNextHash()).thenReturn(generatedHash);
        Url savedUrl = Url.builder().url("http://10.0.0.5").hash(generatedHash).build();
        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);

        String result = urlService.generateShortUrl("http://10.0.0.5");

        assertTrue(result.endsWith("/" + generatedHash));
        verify(hostAddressResolver, never()).resolve(anyString());
    }

    @Test
    void generateShortUrl_shouldRejectCgNatAddress_whenPrivateTargetsDisallowed() throws Exception {
        when(hostAddressResolver.resolve("cgnat.example.com"))
                .thenReturn(new InetAddress[]{InetAddress.getByName("100.64.0.1")});

        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://cgnat.example.com"));

        assertEquals("Private network addresses are not allowed", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowInvalidUrlException_whenUrlTooLong() {
        String longPath = "a".repeat(2048);

        InvalidUrlException exception = assertThrows(InvalidUrlException.class,
                () -> urlService.generateShortUrl("http://example.com/" + longPath));

        assertEquals("URL must not exceed 2048 characters", exception.getMessage());
    }

    @Test
    void generateShortUrl_shouldThrowRuntimeException_whenHashCacheExhausted() {
        when(hashCache.getNextHash()).thenReturn("");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> urlService.generateShortUrl("http://example.com"));

        assertEquals("Failed to generate hash for URL", exception.getMessage());
        verify(urlRepository, never()).save(any(Url.class));
    }
}
