package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.dto.UrlViewDto;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.mapper.UrlMapper;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.util.AfterCommitManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Сервис для взаимодействия с URL
 *
 * @author Linempy
 * @since 13.09.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final UrlCacheRepository cacheRepository;
    private final LocalCache localCache;
    private final UrlMapper mapper;
    private final AfterCommitManager afterCommitManager;

    @Transactional
    public UrlViewDto createShortUrl(UrlCreateDto createDto) {
        String originUrl = createDto.longUrl();

        Optional<UrlEntity> urlEntity = urlRepository.findByUrl(originUrl);
        if (urlEntity.isPresent()) {
            return mapper.toViewDto(urlEntity.get());
        }

        String hash = localCache.getHash();
        UrlEntity url = mapper.toEntity(createDto, hash);
        urlRepository.save(url);

        log.info("Ассоциация хэша и URL была сохранена в бд!");
        afterCommitManager.executeAfterCommit(() -> cacheRepository.save(url));
        return mapper.toViewDto(hash);
    }

    public String getOriginUrl(String hash) {
        return cacheRepository.findOriginUrlByHash(hash)
                .orElseGet(() -> getFromDbAndSaveCache(hash));
    }

    private String getFromDbAndSaveCache(String hash) {
        UrlEntity url = urlRepository.findByIdOrThrows(hash);
        cacheRepository.save(url);
        return url.getUrl();
    }
}