package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.UrlEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для взаимодействия с Redis кэшем
 *
 * @author Linempy
 * @since 14.09.2025
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UrlCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.redis.keys.cache-repository}")
    private String cacheRepositoryKey;

    public void save(UrlEntity url) {
        redisTemplate.opsForHash().put(cacheRepositoryKey, url.getHash(), url.getUrl());
        log.info("URL с хэшем: {} был сохранен в кэш", url.getHash());
    }

    public Optional<String> findOriginUrlByHash(String hash) {
        String originUrl = (String) redisTemplate.opsForHash().get(cacheRepositoryKey, hash);
        return Optional.ofNullable(originUrl);
    }
}