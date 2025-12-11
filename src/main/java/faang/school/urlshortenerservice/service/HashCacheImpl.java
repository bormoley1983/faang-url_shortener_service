package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashCacheImpl implements HashCache {
    private final HashGenerator hashGenerator;
    private final HashRepository hashRepository;

    @Qualifier("hashCacheExecutor")
    private final ExecutorService executorService;

    @Value("${hash.cache.capacity}")
    private int capacity;

    @Value("${hash.cache.fill-percent}")
    private int fillPercent;

    private final Queue<String> hashes = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean filling = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        log.info("Initializing HashCache with capacity: {}, fill percent: {}", capacity, fillPercent);
        fillCacheAsync();
    }

    @Override
    public String getHash() {
        if (needRefill()) {
            fillCacheAsync();
        }

        String hash = hashes.poll();
        if (hash == null) {
            log.error("Hash cache is empty, returning null");
            throw new IllegalStateException("No free hashes available");
        }
        return hash;
    }

    private boolean needRefill() {
        double currentPercent = (double) hashes.size() / capacity * 100;
        boolean needRefill = currentPercent < fillPercent;
        if (needRefill) {
            log.debug("Cache needs refill: {}% < {}%", currentPercent, fillPercent);
        }

        return needRefill;
    }

    private void fillCacheAsync() {
        if (filling.compareAndSet(false, true)) {
            executorService.submit(() -> {
                try {
                    log.info("Filling cache: {}/{}", hashes.size(), capacity);

                    List<String> batch = hashRepository.getHashBatch();
                    if (!batch.isEmpty()) {
                        hashes.addAll(batch);
                        log.info("Loaded {} hashes from DB into cache", batch.size());
                    }

                    hashGenerator.generateBatch()
                            .thenAccept(newHashes -> {
                                if (newHashes != null && !newHashes.isEmpty()) {
                                    hashes.addAll(newHashes);
                                    log.info("Added {} generated hashes into cache", newHashes.size());
                                }
                            })
                            .exceptionally(ex -> {
                                log.error("Failed to generate hashes", ex);
                                return null;
                            });

                } catch (Exception e) {
                    log.error("Cache fill failed", e);
                } finally {
                    filling.set(false);
                }
            });
        }
    }
}