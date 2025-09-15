package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.LocalHashCache;
import faang.school.urlshortenerservice.config.property.UrlProps;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {
    private final UrlProps urlProps;
    private final LocalHashCache localHashCache;
    private final HashRepository hashRepository;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final Executor hashTaskExecutor;

    public UrlDto getShortUrl(UrlDto urlDto) {
        String hash = localHashCache.getHash();
        urlCacheRepository.save(createUrl(hash, urlDto.url()));
        String shortUrl = urlProps.baseShortUrl() + hash;

        return new UrlDto(shortUrl, hash);
    }

    public String getOriginalUrl(String hash) {
        String originalUrl = urlCacheRepository.getOriginalUrl(hash);
        CompletableFuture.runAsync(() -> refreshUrlActivity(hash), hashTaskExecutor);
        return originalUrl;
    }

    @Transactional
    public void cleanExpiredUrlAndReleaseHashes() {
        LocalDateTime expirationDate = LocalDateTime.now().minus(urlProps.expiration().time(),
                                                                 urlProps.expiration().unit());
        List<String> hashes = urlRepository.cleanExpiredAndGetHashes(expirationDate,
                                                                     urlProps.expiration().minRequestCount());
        log.info("Expired urls deleted from DB");

        if (!hashes.isEmpty()) {
            log.info("Saving released hashes after cleaning");
            hashRepository.saveBatch(hashes.toArray(String[]::new));
            hashes.forEach(urlCacheRepository::delete);
        }
    }

    private void refreshUrlActivity(String hash) {
        log.debug("Updating last request date url by hash = {}", hash);
        urlRepository.refreshActivity(hash);
    }

    private Url createUrl(String hash, String url) {
        return Url.builder()
                .hash(hash)
                .url(url)
                .requestCount(1L)
                .lastRequestedAt(LocalDateTime.now())
                .build();
    }
}
