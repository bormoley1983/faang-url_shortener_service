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
    @Value("${expire.time:1}") //todo is working?
    private static final long DEFAULT_EXPIRATION_TIME_IN_YEAR;
    private final UrlRepository urlRepository;
    private final UrlValidator urlValidator;
    private final RedisUrlCacheService redisUrlCacheService;


    public ShortUrl findByHash(String hash) {
        return urlRepository.findByHash(hash)
                .orElseThrow(() -> new RecordNotFoundException("Invalid short url, not found"));
    }

    public ShortUrl getActualurl(String hash) {
        Optional<ShortUrl> redisShortUrl = redisUrlCacheService.getUrl(hash);

        if (redisShortUrl.isPresent()) {
            log.debug("Url from cache");
            return redisShortUrl.get();
        }

        ShortUrl shortUrl = findByHash(hash);
        redisUrlCacheService.cacheUrl(shortUrl);

        urlValidator.validateUrlNotExpired(shortUrl);

        log.debug("Url from db");
        return shortUrl;
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
            redisUrlCacheService.deleteUrlsFromCache(expiredHashes);
        }
    }

    private LocalDateTime setExpireTimeOrDefault(ShortUrlRequest request) {
        LocalDateTime maxExpireTime = LocalDateTime.now().plusYears(DEFAULT_EXPIRATION_TIME_IN_YEAR);
        boolean defaultRequired = request.expireTime() == null || request.expireTime().isAfter(maxExpireTime);

        return defaultRequired ? maxExpireTime : request.expireTime();
    }
}
