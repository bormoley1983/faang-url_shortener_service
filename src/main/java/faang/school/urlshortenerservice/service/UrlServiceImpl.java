package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.CreateUrlDto;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final HashCache hashCache;

    @Override
    public String getOriginalUrl(String hash) {
        log.info("Getting original URL for hash: {}", hash);
        String url = urlCacheRepository.get(hash);
        if (url != null && !url.isBlank()) {
            log.info("Found URL in cache for hash: {}", hash);
            return url;
        }
        log.debug("URL not found in cache: {}", hash);

        url = urlRepository.findUrl(hash)
                .orElseThrow(() -> {
                    log.warn("URL not found in database for hash: {}", hash);
                    return new UrlNotFoundException("URL not found for hash: " + hash);
                });

        log.info("Found URL in database for hash: {}", hash);

        urlCacheRepository.save(hash, url);
        log.info("Saved URL to cache for hash: {}", hash);

        return url;
    }

    @Override
    public String createShortUrl(CreateUrlDto createUrlDto) {
        String url = createUrlDto.url();
        log.info("Creating short URL for url: {}", url);

        String hash = hashCache.getHash();
        log.info("Got hash from HashCache: {}", hash);

        urlRepository.save(hash, url);
        log.info("Saved URL to database for hash: {}", hash);

        urlCacheRepository.save(hash, url);
        log.info("Saved URL to cache for hash: {}", hash);
        return hash;
    }
}