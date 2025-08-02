package faang.school.urlshortenerservice.service;

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
        log.info("Found url '{}' in Redis.", url);
        if (url != null) return url;

        log.info("URL not found in Redis. Falling back to DB.");
        return urlRepository.findLongUrlByHash(hash)
                .orElseThrow(() -> new UrlNotFoundException("No URL found for hash: " + hash));
    }
}