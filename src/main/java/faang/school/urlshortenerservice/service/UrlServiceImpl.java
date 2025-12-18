package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.UrlServiceProperties;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.exception.HashUnavailableException;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.repository.redis.UrlCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {
    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final UrlServiceProperties urlServiceProperties;

    @Override
    @Transactional
    public String createShortUrl(String longUrl) {
        String hash;
        for (int attempt = 1; attempt <= 3; attempt++) {
            hash = hashCache.getHash();
            if (hash == null || hash.isBlank()) {
                log.warn("Hash unavailable, retrying ({}/{})", attempt, 3);
                continue;
            }
            try {
                urlRepository.save(new UrlEntity(hash, longUrl));
            } catch (DataIntegrityViolationException e) {
                log.warn("DB unique constraint violation for url {}, retrying ({}/{})", longUrl, attempt, 3);
                continue;
            }
            try {
                urlCacheRepository.save(hash, longUrl);
            } catch (RedisConnectionFailureException | RedisSystemException e) {
                log.error("Failed to save hash {} to cache", hash, e);
            }
            return urlServiceProperties.getBaseUrl() + "/" + hash;
        }
        throw new HashUnavailableException();
    }
}
