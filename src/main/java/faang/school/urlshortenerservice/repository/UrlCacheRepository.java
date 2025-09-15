package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {
    private final UrlRepository urlRepository;

    @CachePut(value = "url", key = "#url.hash")
    public String save(Url url) {
        urlRepository.save(url);
        return url.getUrl();
    }

    @Cacheable(value = "url", key = "#hash")
    public String getOriginalUrl(String hash) {
        Url url = findByHash(hash);
        return url.getUrl();
    }

    @CacheEvict(value = "url", key = "#hash")
    public void delete(String hash) {
        log.info("Delete from cache hash = {}", hash);
    }

    private Url findByHash(String hash) {
        return urlRepository.findById(hash)
                .orElseThrow(() -> new EntityNotFoundException("Hash {} not found", hash));
    }
}
