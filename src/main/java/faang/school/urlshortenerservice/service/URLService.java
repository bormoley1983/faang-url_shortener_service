package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.cache.HashCache;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class URLService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    @Transactional
    public String createHash(String url) {
        String hash = hashCache.getHash();
        saveCacheForDB(hash, url);
        saveCacheForRedis(hash, url);
        return hash;
    }

    private void saveCacheForDB(String hash, String url) {
        urlRepository.save(new Url(hash, url));
    }

    private void saveCacheForRedis(String hash, String url) {
        urlCacheRepository.save(hash, url);
    }
}
