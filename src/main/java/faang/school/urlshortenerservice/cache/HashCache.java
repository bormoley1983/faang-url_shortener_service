package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.config.UrlShortenerProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.HashGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class HashCache {

    private final BlockingQueue<String> hashQueue;
    private final ExecutorService executorService;
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    private final int maxCacheSize;
    private final double refillThresholdPercent;

    public HashCache(
            @Qualifier("hashCacheExecutor") ExecutorService executorService,
            HashRepository hashRepository,
            HashGenerator hashGenerator,
            UrlShortenerProperties properties) {

        this.executorService = executorService;
        this.hashRepository = hashRepository;
        this.hashGenerator = hashGenerator;
        this.maxCacheSize = properties.getHashCache().getMaxSize();
        this.refillThresholdPercent = properties.getHashCache().getRefillThresholdPercent();
        this.hashQueue = new LinkedBlockingQueue<>(maxCacheSize);

        log.info("HashCache initialized with max-size: {}, refill-threshold: {}%",
                maxCacheSize, refillThresholdPercent * 100);
    }

    @PostConstruct
    public void initializeCache() {
        log.info("Initializing hash cache on startup");
        if (isRefilling.compareAndSet(false, true)) {
            executorService.submit(this::refillCacheAsync);
        }
    }

    public String getHash() {
        int currentSize = hashQueue.size();
        int refillThreshold = (int) (maxCacheSize * refillThresholdPercent);

        log.debug("Current cache size: {}, refill threshold: {}", currentSize, refillThreshold);

        if (currentSize < refillThreshold && isRefilling.compareAndSet(false, true)) {
            log.info("Cache size {} is below threshold {}, starting async refill", currentSize, refillThreshold);
            executorService.submit(this::refillCacheAsync);
        }

        String hash = hashQueue.poll();
        log.debug("Retrieved hash from cache: {}, remaining cache size: {}", hash, hashQueue.size());
        return hash;
    }

    private void refillCacheAsync() {
        try {
            log.info("Starting async cache refill process");

            int neededHashes = maxCacheSize - hashQueue.size();
            List<String> hashes = hashRepository.getHashBatch(neededHashes);

            if (!hashes.isEmpty()) {
                for (String hash : hashes) {
                    hashQueue.offer(hash);
                }

                log.info("Added {} hashes to cache. Current cache size: {}",
                        hashes.size(), hashQueue.size());
            } else {
                log.warn("No available hashes found in database");
            }

            executorService.submit(() -> {
                try {
                    hashGenerator.generateBatch().get();
                    log.info("Successfully generated additional hashes in database");
                } catch (Exception e) {
                    log.error("Failed to generate additional hashes", e);
                }
            });

        } catch (Exception e) {
            log.error("Failed to refill hash cache", e);
        } finally {
            isRefilling.set(false);
            log.debug("Cache refill process completed, lock released");
        }
    }
}
