package faang.school.urlshortenerservice.unitTests.service;

import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.service.LocalCache;
import faang.school.urlshortenerservice.service.UrlServiceImpl;
import faang.school.urlshortenerservice.util.AfterCommitManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static faang.school.urlshortenerservice.data.UrlControllerTestData.ORIGIN_URL;
import static faang.school.urlshortenerservice.data.UrlServiceImplTestData.DEFAULT_HASH;
import static faang.school.urlshortenerservice.data.UrlServiceImplTestData.getDefaultUrlEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Тестирование класса {@link UrlServiceImplTest}
 *
 * @author Linempy
 * @since 16.09.2025
 */
@DisplayName("Тестирование UrlServiceImpl")
@ExtendWith(MockitoExtension.class)
public class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlCacheRepository cacheRepository;

    @Mock
    private LocalCache localCache;

    @Spy
    private UrlMapper mapper;

    @Mock
    private AfterCommitManager afterCommitManager;

    @InjectMocks
    private UrlServiceImpl service;

    @Test
    @DisplayName("Проверка на получения короткой ссылки, если оригинальная уже имеет хэш")
    void shouldCreateShortUrlWhenIsExists() {
        UrlCreateDto createDto = new UrlCreateDto(ORIGIN_URL);
        UrlEntity hash = getDefaultUrlEntity();
        when(urlRepository.findByUrl(ORIGIN_URL)).thenReturn(Optional.of(hash));

        service.createShortUrl(createDto);

        assertThat(createDto.longUrl()).isEqualTo(ORIGIN_URL);
        verify(urlRepository, times(1)).findByUrl(createDto.longUrl());
    }

    @Test
    @DisplayName("Проверка что возвращается сокращенная ссылка, если в БД еще нет такого URL")
    void shouldCreateShortUrlWhenNotExists() {
        UrlCreateDto createDto = new UrlCreateDto(ORIGIN_URL);
        when(urlRepository.findByUrl(ORIGIN_URL)).thenReturn(Optional.empty());
        when(localCache.getHash()).thenReturn(DEFAULT_HASH);

        service.createShortUrl(createDto);

        ArgumentCaptor<UrlEntity> entityCaptor = ArgumentCaptor.forClass(UrlEntity.class);

        verify(urlRepository, times(1)).findByUrl(createDto.longUrl());
        verify(localCache, times(1)).getHash();
        verify(urlRepository, times(1)).save(entityCaptor.capture());
        verify(afterCommitManager, times(1)).executeAfterCommit(any(Runnable.class));

        UrlEntity savedEntity = entityCaptor.getValue();
        assertThat(savedEntity.getUrl()).isEqualTo(ORIGIN_URL);
        assertThat(savedEntity.getHash()).isEqualTo(DEFAULT_HASH);
    }

    @Test
    @DisplayName("Проверка на получение оригинального url, когда его нет в кэше")
    void shouldGetOriginUrlWhenNotInCache() {
        UrlEntity urlEntity = getDefaultUrlEntity();
        when(cacheRepository.findOriginUrlByHash(DEFAULT_HASH)).thenReturn(Optional.empty());
        when(urlRepository.findByIdOrThrows(DEFAULT_HASH)).thenReturn(urlEntity);

        String result = service.getOriginUrl(DEFAULT_HASH);

        assertThat(result).isEqualTo(ORIGIN_URL);
        verify(cacheRepository, times(1)).findOriginUrlByHash(DEFAULT_HASH);
        verify(urlRepository, times(1)).findByIdOrThrows(DEFAULT_HASH);
        verify(cacheRepository, times(1)).save(urlEntity);
    }

    @Test
    @DisplayName("Должен вернуть сокращенную ссылку, если есть в кэше")
    void shouldReturnUrlFromCacheWhenExists() {
        when(cacheRepository.findOriginUrlByHash(DEFAULT_HASH))
                .thenReturn(Optional.of(ORIGIN_URL));

        String result = service.getOriginUrl(DEFAULT_HASH);

        assertThat(result).isEqualTo(ORIGIN_URL);
        verify(cacheRepository, times(1)).findOriginUrlByHash(DEFAULT_HASH);
        verifyNoInteractions(urlRepository);
    }
}