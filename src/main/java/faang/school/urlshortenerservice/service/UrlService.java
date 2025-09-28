package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.dto.CreateUrlDto;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.mapper.UrlMapper;
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
    private final UrlMapper urlMapper;

    public String createShortUrl(CreateUrlDto createUrlDto) {
        CompletableFuture<String> hashFuture = hashCache.getHash()
                .thenApply(h -> {
                    String originalUrl = createUrlDto.url();
                    log.info("Получен хеш {} для URL {}", h,
                            originalUrl);

                    Url url = urlMapper.toUrl(createUrlDto);
                    url.setHash(h);

                    urlRepository.save(url);
                    log.info("Ассоциация хеша {} с URL {} сохранена в БД", h,
                            originalUrl);

                    urlCacheRepository.saveUrl(h, originalUrl);
                    log.info("Ассоциация хеша {} с URL {} сохранена в кэше",
                            h, originalUrl);

                    return h;
                });

        log.info("Подготовка ответа для контроллера");
        return Hash.builder()
                .hashValue(hashFuture.join())
                .build()
                .getHashValue();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "urls", key = "#hash")
    public String getUrl(String hash) {
        String url = urlCacheRepository.findUrlByHash(hash);

        if (url != null) {
            log.info("URL для хеша {} найден в кэше", hash);
            return url;
        }

        log.info("URL для хеша {} не найден в кэше, ищем в БД", hash);
        Url urlEntity = urlRepository.findByHash(hash)
                .orElseThrow(() -> new UrlNotFoundException(hash));

        urlCacheRepository.saveUrl(hash, urlEntity.getOriginalUrl());

        return urlEntity.getOriginalUrl();
    }
}
