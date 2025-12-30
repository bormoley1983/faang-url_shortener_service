package faang.school.urlshortenerservice;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlShortenerException;
import faang.school.urlshortenerservice.hash.HashCache;
import faang.school.urlshortenerservice.mapper.HashMapper;
import faang.school.urlshortenerservice.repo.UrlCacheRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import faang.school.urlshortenerservice.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private HashMapper hashMapper;

    @InjectMocks
    private UrlService urlService;

    private final String SHORT_URL = "abc123";
    private final String LONG_URL = "https://example.com/very/long/url";
    private Url url;
    private LongUrlDto longUrlDto;

    @BeforeEach
    void setUp() {
        url = new Url(SHORT_URL, LONG_URL);
        longUrlDto = new LongUrlDto(LONG_URL);
    }

    @Test
    void getOriginUrl_ShouldReturnLongUrlDtoFromCache() {
        when(urlCacheRepository.findLongUrl(SHORT_URL)).thenReturn(Optional.of(LONG_URL));

        LongUrlDto result = urlService.getOriginUrl(SHORT_URL);

        verify(urlCacheRepository).findLongUrl(SHORT_URL);
        verifyNoInteractions(urlRepository);

        assertEquals(LONG_URL, result.longUrl());
    }

    @Test
    void getOriginUrl_ShouldLoadFromDbIfCacheMiss() {
        when(urlCacheRepository.findLongUrl(SHORT_URL)).thenReturn(Optional.empty());
        when(urlRepository.findByShortUrl(SHORT_URL)).thenReturn(Optional.of(url));

        LongUrlDto result = urlService.getOriginUrl(SHORT_URL);

        verify(urlCacheRepository).findLongUrl(SHORT_URL);
        verify(urlRepository).findByShortUrl(SHORT_URL);
        verify(urlCacheRepository).save(SHORT_URL, LONG_URL);

        assertEquals(LONG_URL, result.longUrl());
    }

    @Test
    void getOriginUrl_ShouldThrowExceptionIfNotFound() {
        when(urlCacheRepository.findLongUrl(SHORT_URL)).thenReturn(Optional.empty());
        when(urlRepository.findByShortUrl(SHORT_URL)).thenReturn(Optional.empty());

        assertThrows(UrlShortenerException.class, () -> urlService.getOriginUrl(SHORT_URL));
    }
}