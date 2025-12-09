package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final BlockingDeque<String> cache;

    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @Value("${url-shortener.hash-cache.size:1000)")
    private int cacheMaxSize;

    @Value("${url-shortener.hash-cache.refill-threshold-percent:20)")
    private int refillThresholdPercent;

    public String getHash() {

        int threshold = cacheMaxSize * refillThresholdPercent / 100;

        if (cache.size() < threshold) {
            asyncRefill();
        }

        return cache.pollFirst();
    }

    @Async("hashCacheExecutor")
    public void asyncRefill() {
        if (!isRefilling.compareAndSet(false, true)) {
            return;
        }

        try {
            int needed = cacheMaxSize - cache.size();
            if (needed <= 0) return;

            List<String> hashes = hashRepository.getHashBatch(needed);
            cache.addAll(hashes);

            log.info("Refilled {} hashes from DB", hashes.size());

            hashGenerator.generateBatch();

        } catch (Exception e) {
            log.error("HashCache async refill error", e);
        } finally {
            isRefilling.set(false);
        }
    }
}
