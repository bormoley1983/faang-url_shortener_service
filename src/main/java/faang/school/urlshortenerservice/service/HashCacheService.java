package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class HashCacheService {
    private final HashDao hashDao;
    private final HashGenerator hashGenerator;
    private final Executor hashCacheExecutor;

    public HashCacheService(HashDao hashDao,
                            HashGenerator hashGenerator,
                            @Qualifier("hashCacheExecutor") Executor hashCacheExecutor) {
        this.hashDao = hashDao;
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
        if (hash != null) return hash;

        log.warn("Hash cache is empty. Attempting emergency sync refill...");
        if (refillCacheSync() == 0) {
            throw new IllegalStateException("No available hashes after emergency refill.");
        }

        return Optional.ofNullable(hashQueue.poll())
                .orElseThrow(() -> new IllegalStateException("Refill succeeded but no hash was retrieved."));
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
                    refillCacheSync();
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

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpOnStartup() {
        log.info("Warming up hash cache on startup...");
        hashGenerator.generateBatch();
        int added = refillCacheSync();

        if (added == 0) {
            throw new IllegalStateException("Failed to warm up hash cache on startup");
        }

        log.info("Cache warm-up successful. Hashes ready: {}", hashQueue.size());
    }

    private int refillCacheSync() {
        log.info("Refilling hash cache from DB");
        int needed = Math.min(reloadBatchSize, maxCacheSize - hashQueue.size());
        List<String> hashes = hashDao.getHashBatch(needed);
        hashes.forEach(hashQueue::offer);
        log.info("Hash cache refill complete. Added {} hashes. Total size: {}", hashes.size(), hashQueue.size());
        return hashes.size();
    }
}