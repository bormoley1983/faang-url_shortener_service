package faang.school.urlshortenerservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void save(String hash, String url) {
        stringRedisTemplate.opsForValue().set(hash, url);
    }

    @Override
    public Optional<String> getUrlByHash(String hash) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(hash));
    }
}
