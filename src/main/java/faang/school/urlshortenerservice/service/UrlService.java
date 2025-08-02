package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    private final UrlCacheRepository urlCacheRepository;
    private final UrlRepository urlRepository;

    public String getLongUrl(String hash) {
        String url = urlCacheRepository.getLongUrl(hash);
        if (url != null) {
            log.info("Found URL in Redis for hash '{}'", hash);
            return url;
        }

        log.info("URL not found in Redis. Falling back to DB.");
        url = urlRepository.findByHash(hash)
                .map(Url::getUrl)
                .orElseThrow(() -> new UrlNotFoundException("No URL found for hash: " + hash));

        log.info("Caching URL for hash '{}' to Redis", hash);
        urlCacheRepository.cacheLongUrl(hash, url);
        return url;
    }
}