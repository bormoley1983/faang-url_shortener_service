package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.UrlServiceProperties;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.exception.HashUnavailableException;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.repository.redis.UrlCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final int MAX_ATTEMPTS = 3;

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final UrlServiceProperties urlServiceProperties;

    @Override
    @Transactional
    public String createShortUrl(String longUrl) {
        log.debug("Creating short URL");

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String hash = hashCache.getHash();

            if (hash == null || hash.isBlank()) {
                log.warn("Hash unavailable, retrying ({}/{})", attempt, MAX_ATTEMPTS);
                continue;
            }

            try {
                urlRepository.save(new UrlEntity(hash, longUrl));
            } catch (DataIntegrityViolationException e) {
                log.warn("DB hash collision for hash={}, retrying ({}/{})", hash, attempt, MAX_ATTEMPTS);
                continue;
            }

            saveToCacheBestEffort(hash, longUrl);

            return urlServiceProperties.getBaseUrl() + "/" + hash;
        }

        log.error("Failed to generate hash after {} attempts", MAX_ATTEMPTS);
        throw new HashUnavailableException();
    }

    @Override
    public String getOriginalUrl(String hash) {
        log.debug("Resolving original URL for hash={}", hash);

        if (hash == null || hash.isBlank()) {
            log.warn("Invalid hash received: {}", hash);
            throw new UrlNotFoundException("Invalid hash: " + hash);
        }

        Optional<String> cachedUrl = getFromCacheBestEffort(hash);
        if (cachedUrl.isPresent()) {
            log.debug("Cache HIT for hash={}", hash);
            return cachedUrl.get();
        }

        log.debug("Cache MISS for hash={}", hash);

        UrlEntity entity = urlRepository.findById(hash)
                .orElseThrow(() -> {
                    log.warn("URL not found in DB for hash={}", hash);
                    return new UrlNotFoundException("Invalid hash: " + hash);
                });

        String url = entity.getUrl();
        log.info("Loaded URL from DB for hash={}", hash);

        saveToCacheBestEffort(hash, url);
        log.debug("Cached URL for hash={}", hash);

        return url;
    }

    private Optional<String> getFromCacheBestEffort(String hash) {
        try {
            return urlCacheRepository.find(hash);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("Redis unavailable on read for hash={}", hash, e);
            return Optional.empty();
        }
    }

    private void saveToCacheBestEffort(String hash, String url) {
        try {
            urlCacheRepository.save(hash, url);
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("Redis unavailable on write for hash={}", hash, e);
        }
    }
}
