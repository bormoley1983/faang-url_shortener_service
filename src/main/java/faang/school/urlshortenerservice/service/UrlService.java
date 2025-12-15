package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.ShortUrlRequest;
import faang.school.urlshortenerservice.entity.ShortUrl;
import faang.school.urlshortenerservice.exception.RecordNotFoundException;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.validation.UrlValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    @Value("${expire.time:1}")
    private static final long DEFAULT_EXPIRATION_TIME_IN_YEAR;
    private final UrlRepository urlRepository;
    private final UrlValidator urlValidator;
    private final RedisUrlCacheService redisUrlCacheService;


    public ShortUrl findByHash(String hash) {
        return urlRepository.findByHash(hash)
                .orElseThrow(() -> new RecordNotFoundException("Invalid short url, not found"));
    }

    public ShortUrl getUrl(String hash) {
        Optional<ShortUrl> redisUrl = redisUrlCacheService.getUrl(hash);

        if (redisUrl.isPresent()) {
            log.debug("Url from cache");
            return redisUrl.get();
        }

        ShortUrl url = findByHash(hash);
        redisUrlCacheService.cacheUrl(url);

        urlValidator.validateUrlNotExpired(url);

        log.debug("Url from db");
        return url;
    }

    public ShortUrl getShortUrl(ShortUrlRequest request) {
        ShortUrl newShortUrl = ShortUrl.builder()
                .hash(hashCache.getHash())
                .actualUrl(request.url())
                .expire_time(setExpireTimeOrDefault(request))
                .build();

        newShortUrl = urlRepository.save(newShortUrl);
        redisUrlCacheService.cacheUrl(newShortUrl);

        return newShortUrl;
    }

    @Transactional
    public void deleteExpiredShortUrls(int limit) {
        List<String> expiredHashes = urlRepository.findExpiredUrlHashes(limit);
        log.info("Batch contain {} expired hashes", expiredHashes.size());
        if (!expiredHashes.isEmpty()) {
            urlRepository.deleteAllByIdInBatch(expiredHashes);
            redisUrlCacheService.deleteUrlFromCacheAllIn(expiredHashes);
        }
    }

    private LocalDateTime setExpireTimeOrDefault(ShortUrlRequest request) {
        LocalDateTime maxExpireTime = LocalDateTime.now().plusYears(DEFAULT_EXPIRATION_TIME_IN_YEAR);
        boolean defaultRequired = request.expireTime() == null || request.expireTime().isAfter(maxExpireTime);

        return defaultRequired ? maxExpireTime : request.expireTime();
    }
}
