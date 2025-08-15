package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.repo.UrlCacheRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final HashCache hashCache;

    @Value("${url-shortener.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public String createShortUrl(String originalUrl) {
        String hash = hashCache.getHash();

        if (urlRepository.findByHash(hash).isPresent()) {
            throw new RuntimeException("Хэш уже используется: " + hash);
        }

        UrlEntity entity = UrlEntity.builder()
                .hash(hash)
                .originalUrl(originalUrl)
                .build();

        urlRepository.save(entity);
        urlCacheRepository.save(hash, originalUrl);

        return baseUrl + "/url/" + hash;
    }

    @Transactional(readOnly = true)
    public String getOriginalUrl(String hash) {
        String url = urlCacheRepository.findByHash(hash);
        if (url != null) {
            return url;
        }

        return urlRepository.findByHash(hash)
                .map(UrlEntity::getOriginalUrl)
                .orElseThrow(() -> new UrlNotFoundException(hash));
    }
}