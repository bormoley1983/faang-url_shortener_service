package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final ExecutorService hashCacheExecutor;

    private final BlockingDeque<String> cache;

    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @Value("${url-shortener.hash-cache.size:1000}")
    private int cacheMaxSize;

    @Value("${url-shortener.hash-cache.refill-threshold-percent:20}")
    private int refillThresholdPercent;

    public String getHash() {
        if (cache.isEmpty() || cache.size() < cacheMaxSize * refillThresholdPercent / 100) {
            triggerAsyncRefill();
        }

        return cache.pollFirst();
    }

    private void triggerAsyncRefill() {
        if (isRefilling.compareAndSet(false, true)) {
            log.info("HashCache: starting async refill");

            hashCacheExecutor.submit(() -> {
                try {
                    int refillSize = cacheMaxSize - cache.size();
                    if (refillSize <= 0) return;

                    List<String> hashesFromDb = hashRepository.getHashBatch(refillSize);
                    cache.addAll(hashesFromDb);

                    log.info("HashCache: refilled {} hashes from DB", hashesFromDb.size());

                    hashGenerator.generateBatch();

                } catch (Exception e) {
                    log.error("HashCache: error during async refill", e);
                } finally {
                    isRefilling.set(false);
                }
            });
        }
    }
}
