package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final BlockingDeque<String> cache = new LinkedBlockingDeque<>();

    private final AtomicBoolean isRefilling = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    @Value("${url-shortener.hash-cache.size:1000}")
    private Integer cacheMaxSize;

    @Value("${url-shortener.hash-cache.refill-threshold-percent:20}")
    private Integer refillThresholdPercent;

    @PostConstruct
    public void init() {
        asyncRefill();
    }

    @Transactional
    public String getHash() {

        int threshold = (cacheMaxSize * refillThresholdPercent) / 100;

        if (cache.size() < threshold) {
            CompletableFuture.runAsync(this::asyncRefill, executor);
        }

        return cache.pollFirst();
    }

    public void asyncRefill() {
        if (!isRefilling.compareAndSet(false, true)) {
            return;
        }

        try {
            hashGenerator.generateBatch();

            int needed = cacheMaxSize - cache.size();
            if (needed <= 0) return;

            List<String> hashes = hashRepository.getHashBatch(needed);
            cache.addAll(hashes);

            log.info("Refilled {} hashes from DB", hashes.size());

        } catch (Exception e) {
            log.error("HashCache async refill error", e);
        } finally {
            isRefilling.set(false);
        }
    }
}