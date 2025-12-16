package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Url;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository{
    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String hash, String longUrl) {
        redisTemplate.opsForValue().set(hash, longUrl);
    }


    @Override
    public Url findLongUrl(String hash) {
        Optional<String> longUrl = Optional.ofNullable(redisTemplate.opsForValue().get(hash));
        return new Url(hash, longUrl.get());
    }
}
