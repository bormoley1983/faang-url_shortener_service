package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.NoFreeHashesException;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final HashRepository hashRepository;

    @Value("${url.cleanup.expiration-days}")
    private long expirationDays;

    @Transactional
    public String createShortUrl(String longUrl) {

        String hash = hashCache.getHash();

        Url url = Url.builder()
                .hash(hash)
                .url(longUrl)
                .build();

        urlRepository.save(url);
        urlCacheRepository.save(hash, longUrl);

        return hash;
    }

    @Transactional(readOnly = true)
    public String getShortUrl(String hash) {

        String cached = urlCacheRepository.get(hash);
        if (cached != null) {
            return cached;
        }

        Url url = urlRepository.findById(hash)
                .orElseThrow(() -> new NoFreeHashesException("URL not found for hash: " + hash));

        urlCacheRepository.save(hash, url.getUrl());

        return url.getUrl();
    }

    @Transactional
    public void cleanOldUrls() {

        LocalDateTime expired = LocalDateTime.now().minusDays(expirationDays);

        List<String> freedHashes = urlRepository.deleteOldUrlsAndReturnHashes(expired);

        if (freedHashes.isEmpty()) {
            log.info("CleanerScheduler: no old URLs found for cleanup");
            return;
        }

        hashRepository.returnHashes(freedHashes.toArray(String[]::new));
        log.info("CleanerScheduler: finished. Freed {} hashes", freedHashes.size());
    }
}
