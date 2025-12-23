package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.ShortUrlRequest;
import faang.school.urlshortenerservice.entity.ShortUrl;
import faang.school.urlshortenerservice.exception.RecordNotFoundException;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.validation.UrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    private static final long EXPIRATION_TIME_IN_WEEKS = 1L;
    private final UrlRepository urlRepository;
    private final UrlValidator urlValidator;
    private final HashCache hashCache;
    private final RedisUrlCacheService redisUrlCacheService;

    @Transactional(readOnly = true)
    public ShortUrl getActualUrl(String hash) {
        return redisUrlCacheService.getUrl(hash)
                .orElseGet(() -> {
                    ShortUrl shortUrl = findByHash(hash);
                    redisUrlCacheService.cacheUrl(shortUrl);
                    log.debug("Url from db {}", shortUrl);
                    return shortUrl;
                });
    }

    @Transactional
    public ShortUrl createShortUrl(ShortUrlRequest request) {
        urlValidator.validate(request.url());

        ShortUrl newShortUrl = ShortUrl.builder()
                .hash(hashCache.getHash())
                .actualUrl(request.url())
                .expireTime(LocalDateTime.now().plusWeeks(EXPIRATION_TIME_IN_WEEKS))
                .build();

        newShortUrl = urlRepository.save(newShortUrl);
        redisUrlCacheService.cacheUrl(newShortUrl);
        log.info("Created short URL: {} -> {}", newShortUrl.getHash(), request.url());

        return newShortUrl;
    }

    @Transactional
    public int deleteExpiredShortUrls(int limit) {
        List<String> expiredHashes = urlRepository.findExpiredUrlHashes(limit);
        int deletedCount = expiredHashes.size();
        log.info("Batch contain {} expired hashes", deletedCount);

        if (!expiredHashes.isEmpty()) {
            urlRepository.deleteAllByIdInBatch(expiredHashes);
            redisUrlCacheService.deleteUrlsFromCache(expiredHashes);
            hashCache.returnHashes(expiredHashes);
        }
        return deletedCount;
    }

    private ShortUrl findByHash(String hash) {
        return urlRepository.findByHash(hash)
                .orElseThrow(() -> new RecordNotFoundException("Invalid short url, not found"));
    }
}
