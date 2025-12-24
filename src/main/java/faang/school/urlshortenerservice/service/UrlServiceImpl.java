package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.DataNotFoundException;
import faang.school.urlshortenerservice.properties.CleanerProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final HashRepository hashRepository;
    private final UrlRepository urlRepository;
    private final CleanerProperties cleanerProperties;
    private final UrlCacheRepository urlCacheRepository;
    private final HashCache hashCache;

    @Override
    @Transactional
    public void cleanOldUrls() {
        log.info("Starting cleanup of old URLs");

        LocalDateTime cutoffDate = LocalDateTime.now()
                .minusYears(cleanerProperties.getOlderThanYears());

        log.debug("Deleting URLs older than {}", cutoffDate);

        List<String> hashes = urlRepository.deleteOldUrlsAndReturnHashes(cutoffDate);

        if (hashes.isEmpty()) {
            log.info("No old URLs found to clean up");
            return;
        }

        log.info("Deleted {} old URLs", hashes.size());

        hashRepository.save(hashes);
        log.info("Saved {} hashes back to hash table", hashes.size());

        log.info("Cleanup completed successfully");
    }

    @Override
    public UrlDto getUrl(String hash) {

        Optional<String> cachedUrl = urlCacheRepository.getUrlByHash(hash);
        if (cachedUrl.isPresent()) {
            log.info("Found url in Redis for hash: {}", hash);
            return new UrlDto(cachedUrl.get());
        }

        Optional<String> dbUrl = urlRepository.findUrlByHash(hash);
        if (dbUrl.isPresent()) {
            String url = dbUrl.get();
            log.info("Found url in database for hash: {}", hash);
            urlCacheRepository.save(hash, url);
            return new UrlDto(url);
        }

        log.warn("URL not found for hash: {}", hash);
        throw new DataNotFoundException("URL not found for hash: " + hash);
    }

    @Override
    @Transactional
    public String createShortUrl(UrlDto urlDto) {
        log.info("Creating short URL for: {}", urlDto.url());

        String hash = hashCache.getHash();
        log.debug("Retrieved hash from cache: {}", hash);

        urlRepository.save(hash, urlDto.url());
        log.debug("Saved to database: {} -> {}", hash, urlDto.url());

        urlCacheRepository.save(hash, urlDto.url());
        log.debug("Saved to Redis cache: {} -> {}", hash, urlDto.url());

        log.info("Created short URL with hash: {}", hash);

        return hash;
    }
}
