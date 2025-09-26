package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.config.UrlShortenerProperties;
import faang.school.urlshortenerservice.dto.UrlResponseDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.HashNotAvailableException;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final UrlShortenerProperties properties;

    @Transactional
    public UrlResponseDto createShortUrl(String originalUrl) {
        log.info("Creating short URL for: {}", originalUrl);

        String hash = hashCache.getHash();
        if (hash == null) {
            log.error("No hash available from cache for URL: {}", originalUrl);
            throw new HashNotAvailableException("No hash available from cache");
        }

        Url urlEntity = Url.builder()
                .hash(hash)
                .url(originalUrl)
                .build();

        Url savedUrl = urlRepository.save(urlEntity);
        log.debug("Saved URL to database with hash: {}", savedUrl.getHash());

        urlCacheRepository.save(hash, originalUrl);
        log.debug("Saved URL mapping to Redis cache: {} -> {}", hash, originalUrl);

        String shortUrl = properties.getDomain() + "/" + hash;

        UrlResponseDto response = UrlResponseDto.builder()
                .shortUrl(shortUrl)
                .originalUrl(originalUrl)
                .hash(hash)
                .build();

        log.info("Successfully created short URL: {} for original URL: {}", shortUrl, originalUrl);
        return response;
    }

    /**
     * Получает оригинальный URL по хешу, сначала из Redis, затем из БД
     *
     * @param hash хеш короткой ссылки
     * @return оригинальный URL
     * @throws UrlNotFoundException если URL не найден ни в кеше, ни в БД
     */
    public String getOriginalUrl(String hash) {
        log.info("Looking for original URL by hash: {}", hash);

        return urlCacheRepository.findByHash(hash)
                .map(url -> {
                    log.debug("Found URL in Redis cache: {} -> {}", hash, url);
                    return url;
                })
                .orElseGet(() -> {
                    log.debug("URL not found in Redis cache, searching in database for hash: {}", hash);

                    return urlRepository.findByHash(hash)
                            .map(urlEntity -> {
                                String originalUrl = urlEntity.getUrl();
                                log.debug("Found URL in database: {} -> {}", hash, originalUrl);

                                urlCacheRepository.save(hash, originalUrl);
                                log.debug("Cached URL in Redis: {} -> {}", hash, originalUrl);

                                return originalUrl;
                            })
                            .orElseThrow(() -> {
                                log.warn("URL not found for hash: {}", hash);
                                return new UrlNotFoundException("URL not found for hash: " + hash);
                            });
                });
    }
}