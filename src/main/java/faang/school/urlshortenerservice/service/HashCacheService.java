package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class HashCacheService {
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final Executor hashCacheExecutor;

    public HashCacheService(HashRepository hashRepository,
                            HashGenerator hashGenerator,
                            @Qualifier("hashCacheExecutor") Executor hashCacheExecutor) {
        this.hashRepository = hashRepository;
        this.hashGenerator = hashGenerator;
        this.hashCacheExecutor = hashCacheExecutor;
    }

    @Value("${hash.cache.max-size}")
    private int maxCacheSize;

    @Value("${hash.cache.reload-threshold}")
    private double reloadThreshold;

    @Value("${hash.cache.reload-batch-size}")
    private int reloadBatchSize;

    private final Queue<String> hashQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isReloading = new AtomicBoolean(false);

    public String getHash() {
        checkAndTriggerRefill();

        String hash = hashQueue.poll();
        if (hash == null) {
            log.error("Hash cache is empty and no hash could be provided");
            throw new IllegalStateException("No available hashes in cache. Refill in progress or failed.");
        }
        return hash;
    }

    private void checkAndTriggerRefill() {
        double currentRatio = (double) hashQueue.size() / maxCacheSize;
        if (currentRatio > reloadThreshold) {
            return;
        }

        if (isReloading.compareAndSet(false, true)) {
            log.info("Triggering async cache refill");
            CompletableFuture.runAsync(() -> {
                try {
                    refillCache();
                    hashGenerator.generateBatch();
                } finally {
                    isReloading.set(false);
                }
            }, hashCacheExecutor).exceptionally(ex -> {
                log.error("Async refill failed", ex);
                return null;
            });
        }
    }

    private void refillCache() {
        log.info("Refilling hash cache from DB");
        int needed = maxCacheSize - hashQueue.size();
        hashRepository.fetchHashes(Math.min(reloadBatchSize, needed))
                .forEach(hashQueue::offer);
        log.info("Hash cache refill complete. New size: {}", hashQueue.size());
    }

    @PostConstruct
    public void init() {
        log.info("Initializing HashCacheService");
        checkAndTriggerRefill();
    }
}