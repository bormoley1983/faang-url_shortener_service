package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class URLServiceTest {
    @Mock
    private HashCache hashCache;
    @Mock
    private UrlRepository urlRepository;
    @Mock
    private UrlCacheRepository urlCacheRepository;
    @InjectMocks
    private URLService service;

    @Test
    @DisplayName("Тестирование создания хэша")
    void createHashTest() {
        when(hashCache.getHash()).thenReturn("abc");
        service.createHash("https://tracker.com/");
        verify(urlCacheRepository).save("abc", "https://tracker.com/");
    }

    @Test
    @DisplayName("Тестирование получения url по хэшу")
    void getUrlTest() {
        when(urlCacheRepository.getCacheUrl("abc")).thenReturn("someUrl");
        assertEquals("someUrl", service.getUrl("abc"));
    }
}
