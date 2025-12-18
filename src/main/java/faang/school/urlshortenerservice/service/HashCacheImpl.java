package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.hash.HashCacheProperties;
import faang.school.urlshortenerservice.repository.db.HashRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class HashCacheImpl implements HashCache {
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final ExecutorService hashCacheExecutor;
    private final BlockingQueue<String> cache;
    private final HashCacheProperties properties;
    private final AtomicBoolean refillInProgress = new AtomicBoolean(false);

    public HashCacheImpl(HashCacheProperties properties,
                         HashRepository hashRepository,
                         HashGenerator hashGenerator,
                         ExecutorService hashCacheExecutor) {
        this.properties = properties;
        this.cache = new LinkedBlockingQueue<>(properties.getCapacity());
        this.hashRepository = hashRepository;
        this.hashGenerator = hashGenerator;
        this.hashCacheExecutor = hashCacheExecutor;
    }

    @Override
    public String getHash() {
        String hash = cache.poll();
        maybeTriggerRefill();
        return hash;
    }

    private void maybeTriggerRefill() {
        int currentSize = properties.getCapacity() - cache.remainingCapacity();
        int threshold = properties.getCapacity()
                * properties.getRefillThresholdPercent() / 100;

        if (currentSize < threshold && refillInProgress.compareAndSet(false, true)) {
            hashCacheExecutor.submit(this::refill);
        }
    }

    /*
        running asynchronously
     */
    private void refill() {
        try {
            int remainingCapacity = cache.remainingCapacity();
            if (remainingCapacity <= 0) {
                return;
            }

            int batchSize = Math.min(remainingCapacity, properties.getRefillBatchSize());

            List<String> hashes = hashRepository.getHashBatch(batchSize);

            for (String hash : hashes) {
                if (!cache.offer(hash)) {
                    break; // queue filled, no need to continue
                }
            }

            // generate hashes (asynchronously)
            hashGenerator.generateBatch()
                    .exceptionally(ex -> {
                        log.error("Hash batch generation failed", ex);
                        return 0;
                    });

        } catch (Exception e) {
            log.error("Failed to refill hash cache", e);
        } finally {
            refillInProgress.set(false);
        }
    }
}
