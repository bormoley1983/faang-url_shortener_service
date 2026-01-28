package faang.school.urlshortenerservice.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final ExecutorService hashCacheExecutor;

    @Value("${hash.cache-refill.size}")
    private int maxSize;

    @Value("${hash.cache-refill.threshold-percent}")
    private int refillThresholdPercent;

    private final BlockingQueue<String> cache = new LinkedBlockingQueue<>();
    private final AtomicBoolean refillInProgress = new AtomicBoolean(false);

    public String getHash() {
        String hash = cache.poll();

        if (hash == null) {
            throw new IllegalStateException("No available hashes in cache");
        }

        checkAndTriggerRefill();
        return hash;
    }

    private void checkAndTriggerRefill() {
        int threshold = maxSize * refillThresholdPercent / 100;

        if (cache.size() > threshold) {
            return;
        }

        if (refillInProgress.compareAndSet(false, true)) {
            log.info("Hash cache below threshold, triggering refill");

            hashCacheExecutor.submit(() -> {
                try {
                    refillCache();
                } finally {
                    refillInProgress.set(false);
                }
            });
        }
    }

    private void refillCache() {
        log.info("Refilling hash cache");

        List<String> hashes = hashRepository.getHashBatch();
        cache.addAll(hashes);

        hashGenerator.generateBatch();

        log.info("Refilled hash cache with {} hashes", hashes.size());
    }
}
