package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Hash;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UrlCacheRepositoryImpl implements UrlCacheRepository{
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(Hash hash) {
        String key = "hash:" + hash.getHash();
        redisTemplate.opsForValue().set(key, hash);
    }

    @Override
    public Hash find(String key) {
        return (Hash) redisTemplate.opsForValue().get("hash:" + key);
    }
}
