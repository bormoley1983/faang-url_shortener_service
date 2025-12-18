package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import faang.school.urlshortenerservice.localcache.LocalCache;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.service.analytic.AnalyticService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private LocalCache localCache;
    @Mock
    private UrlMapper urlMapper;
    @Mock
    private UrlRepository urlRepository;
    @Mock
    private UrlCacheRepository urlCacheRepository;
    @Mock
    private AnalyticService analyticsService;
    @InjectMocks
    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(urlService, "ttlDays", 30L);
    }

    @Test
    void createShortUrl_shouldSaveUrlAndReturnHash() {
        UrlRequestDto dto = new UrlRequestDto("https://example.com");
        Url mappedUrl = new Url();
        String hash = "abc123";

        when(urlMapper.toModel(dto)).thenReturn(mappedUrl);
        when(localCache.getHash()).thenReturn(hash);

        String result = urlService.createShortUrl(dto);

        assertEquals(hash, result);
        assertEquals(hash, mappedUrl.getHash());
        assertNotNull(mappedUrl.getExpiresAt());
        verify(urlRepository).save(mappedUrl);
        verify(urlCacheRepository).save(hash, mappedUrl, 30L);
    }

    @Test
    void getOriginalUrl_shouldReturnUrlFromCacheIfPresent() {
        String hash = "abc123";
        Url cachedUrl = new Url();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(urlCacheRepository.get(hash)).thenReturn(cachedUrl);

        Url result = urlService.getOriginalUrl(hash, request);

        assertSame(cachedUrl, result);
        verify(urlRepository, never()).findByHash(any());
        verify(urlCacheRepository, never()).save(anyString(), any(), anyLong());
        verify(analyticsService).recordClickAsync(cachedUrl, request);
    }

    @Test
    void getOriginalUrl_shouldLoadFromRepoIfCacheMiss() {
        String hash = "abc123";
        Url repoUrl = new Url();
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(urlCacheRepository.get(hash)).thenReturn(null);
        when(urlRepository.findByHash(hash)).thenReturn(Optional.of(repoUrl));

        Url result = urlService.getOriginalUrl(hash, request);

        assertSame(repoUrl, result);
        verify(urlCacheRepository).save(hash, repoUrl, 30L);
        verify(analyticsService).recordClickAsync(repoUrl, request);
    }

    @Test
    void getOriginalUrl_shouldThrowIfNotFound() {
        String hash = "abc123";
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(urlCacheRepository.get(hash)).thenReturn(null);
        when(urlRepository.findByHash(hash)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> urlService.getOriginalUrl(hash, request));
    }
}