package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.LongUrlDto;
import faang.school.urlshortenerservice.dto.ShortUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlShortenerException;
import faang.school.urlshortenerservice.hash.HashCache;
import faang.school.urlshortenerservice.mapper.HashMapper;
import faang.school.urlshortenerservice.repo.UrlCacheRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final HashMapper hashMapper;

    @Value("${retry.maxAttempts}")
    int maxAttempts;

    @Value("${retry.sleep}")
    int sleep;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ShortUrlDto makeShortUrl(LongUrlDto longUrlDto) {
        int attempt = 0;

        while (attempt++ < maxAttempts) {
            Hash hash = hashCache.getHash();

            if (hash == null) {
                log.warn("Hash is null, retrying... attempt {}/{}", attempt, maxAttempts);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new UrlShortenerException("Thread interrupted while waiting for hash", ex);
                }
                continue;
            }

            try {
                Url url = new Url(hash.getHash(), longUrlDto.longUrl());
                urlRepository.save(url);
                registerAfterCommit(hash.getHash(), longUrlDto.longUrl());
                String fullShortUrl = baseUrl + "/" + hash.getHash();

                return new ShortUrlDto(url.getId(), fullShortUrl);
            } catch (DataIntegrityViolationException ex) {
                log.warn("Hash collision for {}. Returning hash to cache and retrying...", hash.getHash());
                hashCache.put(hash);
            } catch (RuntimeException ex) {
                hashCache.put(hash);
                throw ex;
            }
        }
        throw new IllegalStateException("Failed to generate short URL after " + maxAttempts + " retries");
    }

    @Transactional
    public LongUrlDto getOriginUrl(String shortUrl) {
        log.info("Looking up URL for hash: {}", shortUrl);
        Optional<String> cached = urlCacheRepository.findLongUrl(shortUrl);
        log.info("Cache hit: {}", cached.isPresent());
        String longUrl = cached.orElseGet(() -> {
            Url url = urlRepository.findByShortUrl(shortUrl)
                    .orElseThrow(() -> new UrlShortenerException("URL not found"));
            log.info("Loaded from DB: {}", url.getLongUrl());
            urlCacheRepository.save(shortUrl, url.getLongUrl());
            return url.getLongUrl();
        });

        return new LongUrlDto(longUrl);
    }

    private void registerAfterCommit(String hash, String longUrl) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        urlCacheRepository.save(hash, longUrl);
                    }
                }
        );
    }
}

