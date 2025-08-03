package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlCacheRepository urlCacheRepository;
    private final UrlRepository urlRepository;

    public String getOriginalUrl(String hash) {
        log.debug("Looking for URL with hash: {}", hash);

        Optional<String> cachedUrl = urlCacheRepository.findUrlByHash(hash);
        if (cachedUrl.isPresent()) {
            log.debug("Found URL in Redis cache for hash: {}", hash);
            return cachedUrl.get();
        }

        Optional<Url> urlEntity = urlRepository.findByHash(hash);
        if (urlEntity.isPresent()) {
            String originalUrl = urlEntity.get().getUrl();
            log.debug("Found URL in database for hash: {}", hash);

            urlCacheRepository.saveUrl(hash, originalUrl);

            return originalUrl;
        }

        log.warn("URL not found for hash: {}", hash);
        throw new UrlNotFoundException("URL not found for hash: " + hash);
    }
}
