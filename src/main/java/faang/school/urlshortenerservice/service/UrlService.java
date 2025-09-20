package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для управления сокращёнными ссылками.
 * <p>
 * Производит генерацию хэшей, сохранение их в кэш и БД
 * Получение оригинального URL*
 * </p>

 * @author andreyfomchenko
 * @since 12.09.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final CacheService cacheService;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    @Transactional
    public String createShortUrl(String url) {
        String hash = cacheService.getHash();
        UrlEntity urlEntity = UrlEntity.builder()
                .hash(hash)
                .url(url)
                .build();
        urlRepository.save(urlEntity);
        urlCacheRepository.save(url, hash);
        log.info("Создан короткий url {}, из {}", hash, url);
        return hash;
    }

    public String findOriginalUrl(String hash) {
        String url = urlCacheRepository.get(hash);
        if (url != null) {
            log.info("Получен оригинальный URL {} из кэша", url);
            return url;
        }
        UrlEntity urlEntity = urlRepository.findByHashOrElseThrow(hash);
        urlCacheRepository.save(urlEntity.getUrl(), hash);
        log.info("Получен URL из Базы Данных, и сохранен в кэщ");
        return urlEntity.getUrl();
    }
}

