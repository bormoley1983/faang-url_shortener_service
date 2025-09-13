package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.UrlEntity;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Сервис для управления сокращёнными ссылками.
 * <p>
 * Отвечает за генерацию коротких хэшей для длинных URL,
 * сохранение связок в БД и кэше, а также восстановление
 * исходных ссылок по хэшу.
 * </p>
 * <p>
 * Приоритет поиска: сначала Redis, затем БД.
 *
 * @author agent
 * @since 12.09.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCacheService hashCacheService;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    /**
     * Создаёт короткий URL для длинного URL.
     *
     * @param longUrl исходная длинная ссылка
     * @return сгенерированный короткий хэш
     */
    @Transactional
    public String createShortUrl(String longUrl) {
        String hash = hashCacheService.getHash();

        UrlEntity entity = UrlEntity.builder()
                .hash(hash)
                .url(longUrl)
                .build();

        urlRepository.save(entity);
        urlCacheRepository.save(hash, longUrl);

        log.info("Создан короткий URL: {} → {}", hash, longUrl);
        return hash;
    }

    /**
     * Получает исходный URL по хэшу.
     * Сначала проверяет Redis, если не найдено — БД.
     *
     * @param hash хэш короткого URL
     * @return оригинальный URL
     */
    public String getLongUrl(String hash) {

        String url = urlCacheRepository.get(hash);
        if (url != null) {
            log.info("URL {} найден в Redis для хэша {}", url, hash);
            return url;
        }

        UrlEntity entity = urlRepository.findByHashOrElseThrow(hash);

        urlCacheRepository.save(hash, entity.getUrl());
        log.info("URL {} найден в БД и сохранён в Redis для хэша {}", entity.getUrl(), hash);
        return entity.getUrl();
    }
}