package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private static final String REDIS_KEY = "hash:queue";

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final RedisTemplate<String, String> redisTemplate;

    @Qualifier("hashCacheExecutor")
    private final ExecutorService executor;

    private final BlockingDeque<String> localCache = new LinkedBlockingDeque<>();
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @Value("${url-shortener.hash-cache.size:1000}")
    private int localCacheMaxSize;

    @Value("${url-shortener.hash-cache.redis-batch:200}")
    private int redisBatchSize;

    @Value("${url-shortener.hash-cache.refill-threshold-percent:20}")
    private int refillThresholdPercent;

    @PostConstruct
    public void init() {
        triggerRefill();
    }

    public String getHash() {
        String hash = localCache.pollFirst();
        if (hash != null) {
            maybeTriggerRefill();
            return hash;
        }

        hash = popFromRedis();
        if (hash != null) {
            maybeTriggerRefill();
            return hash;
        }

        log.warn("Local + Redis empty → sync fallback");
        return hashGenerator.generateSingleHashSynchronously();
    }

    private void maybeTriggerRefill() {
        int threshold = (localCacheMaxSize * refillThresholdPercent) / 100;
        if (localCache.size() < threshold) {
            triggerRefill();
        }
    }

    private void triggerRefill() {
        if (!isRefilling.compareAndSet(false, true)) {
            return;
        }
        executor.execute(this::refill);
    }

    private void refill() {
        try {
            refillLocalFromRedis();
            refillRedisFromDb();

            hashGenerator.generateBatch();

        } catch (Exception e) {
            log.error("HashCache refill failed", e);
        } finally {
            isRefilling.set(false);
        }
    }

    private String popFromRedis() {
        try {
            return redisTemplate.opsForList().leftPop(REDIS_KEY);
        } catch (Exception e) {
            log.warn("Redis unavailable, skipping", e);
            return null;
        }
    }

    private void refillLocalFromRedis() {
        int needed = localCacheMaxSize - localCache.size();
        if (needed <= 0) return;

        try {
            List<String> batch = redisTemplate.opsForList()
                    .leftPop(REDIS_KEY, Math.min(needed, redisBatchSize));

            if (batch != null && !batch.isEmpty()) {
                localCache.addAll(batch);
                log.info("Loaded {} hashes from Redis to local cache", batch.size());
            }
        } catch (Exception e) {
            log.warn("Redis local refill failed", e);
        }
    }

    private void refillRedisFromDb() {
        Long redisSize;
        try {
            redisSize = redisTemplate.opsForList().size(REDIS_KEY);
        } catch (Exception e) {
            log.warn("Redis size check failed", e);
            return;
        }

        if (redisSize != null && redisSize > redisBatchSize) {
            return;
        }

        List<String> hashes = hashRepository.getHashBatch(redisBatchSize);
        if (hashes.isEmpty()) {
            log.warn("DB hash batch empty");
            return;
        }

        try {
            redisTemplate.opsForList().rightPushAll(REDIS_KEY, hashes);
            log.info("Loaded {} hashes from DB into Redis", hashes.size());
        } catch (Exception e) {
            log.error("Redis push failed", e);
        }
    }
}