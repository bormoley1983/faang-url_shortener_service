package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    public String createShortUrl(String originalUrl) {
        CompletableFuture<String> hashFuture = hashCache.getHash()
                .thenApply(h -> {
                    Url url = Url.builder()
                            .hash(h)
                            .originalUrl(originalUrl)
                            .build();
                    url = urlRepository.save(url);
                    urlCacheRepository.saveUrl(url.getHash(), url.getOriginalUrl());
                    return h;
                });
        return Hash.builder()
                .hashValue(hashFuture.join())
                .build()
                .getHashValue();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#hash")
    public String getUrl(Hash hash) {
        String url = urlCacheRepository.findUrlByHash(hash);

        if (url != null) {
            log.info("URL для хеша {} найден в кэше", hash);
            return url;
        }

        log.info("URL для хеша {} не найден в кэше, ищем в БД", hash);
        Url urlEntity = urlRepository.findByHash(hash)
                .orElseThrow(() -> new UrlNotFoundException(hash));

        urlCacheRepository.saveUrl(hash.getHashValue(), urlEntity.getOriginalUrl());

        return urlEntity.getOriginalUrl();
    }
}

