package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final HashCash hashCash;
    private final UrlCashRepository urlCacheRepository;

    @Value("${url.ttl-days:100}")
    private long ttlDays;

    @Value("${url.prefix-url}")
    private String prefixUrl;

    public String getUrl(String hash) {
        Url url = urlCacheRepository.get(hash);
        if (url == null) {
            return urlRepository.findUrlByHash(hash)
                    .orElseThrow(() -> {
                        log.error("Url linked with {} not found", hash);
                        return new EntityNotFoundException("Url not found");
                    });
        }
        return url.getUrl();
    }

    @Transactional
    public String shortenUrl(UrlDto dto) {
        String shortUrl = hashCash.getHash();
        Url url = Url.builder().hash(shortUrl).url(dto.url()).build();
        urlRepository.save(url);
        urlCacheRepository.save(shortUrl, url, ttlDays);
        log.info("Url shortened to {}", shortUrl);

        return prefixUrl.concat(shortUrl);
    }
}