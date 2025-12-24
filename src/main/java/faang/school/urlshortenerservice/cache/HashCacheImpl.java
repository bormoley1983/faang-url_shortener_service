package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.properties.HashCacheProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class HashCacheImpl implements HashCache {

    private final ConcurrentLinkedQueue<String> cache = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    private final Executor executor;
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final HashCacheProperties cacheProperties;

    public HashCacheImpl(
            @Qualifier("hashCacheExecutor") Executor executor,
            HashRepository hashRepository,
            HashGenerator hashGenerator,
            HashCacheProperties cacheProperties) {
        this.executor = executor;
        this.hashRepository = hashRepository;
        this.hashGenerator = hashGenerator;
        this.cacheProperties = cacheProperties;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing hash cache with max size: {}, threshold: {}%",
                cacheProperties.getCacheSize(),
                cacheProperties.getRefillThresholdPercent());

        List<String> hashes = hashRepository.getHashBatch();
        cache.addAll(hashes);

        log.info("Loaded {} hashes into cache", hashes.size());

        hashGenerator.generateBatch();
        log.info("Started async hash generation");
    }

    @Override
    public String getHash() {
        if (shouldRefillQueue()) {
            log.debug("Cache below threshold, triggering refill");
            refillCache();
        }

        String hash = cache.poll();

        if (hash == null) {
            log.error("Hash cache is empty!");
            throw new IllegalStateException("No hashes available in cache");
        }

        log.debug("Retrieved hash from cache. Number of remaining hashes: {}", cache.size());
        return hash;
    }

    private boolean shouldRefillQueue() {
        int currentSize = cache.size();
        int maxSize = cacheProperties.getCacheSize();
        int thresholdPercent = cacheProperties.getRefillThresholdPercent();
        int threshold = (maxSize * thresholdPercent) / 100;

        boolean needsRefill = currentSize < threshold;

        if (needsRefill) {
            log.debug("Cache below threshold: {} < {} ({}%)", currentSize, threshold, thresholdPercent);
        }

        return needsRefill;
    }

    private void refillCache() {
        if (isRefilling.compareAndSet(false, true)) {
            log.debug("Starting cache refill");
            executor.execute(() -> {
                try {
                    List<String> hashes = hashRepository.getHashBatch();

                    if (hashes.isEmpty()) {
                        log.warn("No hashes available in database!");
                        return;
                    }

                    cache.addAll(hashes);
                    log.info("Refilled cache with {} hashes, new size: {}", hashes.size(), cache.size());

                    hashGenerator.generateBatch();
                    log.debug("Started async hash generation");
                } catch (Exception e) {
                    log.error("Failed to refill cache", e);
                } finally {
                    isRefilling.set(false);
                    log.debug("Cache refill completed");
                }
            });
        } else {
            log.debug("Cache refill already in progress, skipping");
        }
    }
}
