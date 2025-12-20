package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final ExecutorService asyncConfig;

    private final BlockingDeque<String> cache = new LinkedBlockingDeque<>();
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @Value("${url-shortener.hash-cache.size:1000}")
    private Integer cacheMaxSize;

    @Value("${url-shortener.hash-cache.refill-threshold-percent:20}")
    private Integer refillThresholdPercent;

    @PostConstruct
    public void init() {
        hashGenerator.generateBatch();
        refill();
    }

    @Transactional
    public String getHash() {

        int threshold = (cacheMaxSize * refillThresholdPercent) / 100;

        if (cache.size() < threshold) {
           CompletableFuture.runAsync(this::triggerRefill);
           log.info("HashGenerator started batch generation {}", cache.size());
        }

        String hash = cache.pollFirst();

        if (hash == null) {
            log.warn("HashCache empty — generating hash synchronously");
            hash = getHash();
        }

        return hash;
    }

    private void triggerRefill() {
        if (!isRefilling.compareAndSet(false, true)) {
            return;
        }

        CompletableFuture.runAsync(this::refill, asyncConfig);
    }

    public void refill() {
        try {
            int needed = cacheMaxSize - cache.size();
            if (needed <= 0) return;

            List<String> hashes = hashRepository.getHashBatch(needed);
            cache.addAll(hashes);

            log.info("HashCache refill: pulled {} hashes from DB", hashes.size());

            hashGenerator.generateBatch();

        } catch (Exception e) {
            log.error("HashCache async refill error", e);
        } finally {
            isRefilling.set(false);
        }
    }
}