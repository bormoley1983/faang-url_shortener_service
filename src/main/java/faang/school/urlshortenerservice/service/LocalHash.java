package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalHash {
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final Deque<String> hashDeque = new ConcurrentLinkedDeque<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final Semaphore refillSemaphore = new Semaphore(1);

    @Value("${cache.max-size:10000}")
    private int maxCacheSize;

    @Value("${cache.min-threshold-percent:20}")
    private int minThresholdPercent;

    @PostConstruct
    private void initializeCache() {
        try {
            refillCacheSynchronously();
        } catch (Exception e) {
            log.warn("Failed to initialize hash cache", e);
        }
    }

    public String getHash() {
        String hash = hashDeque.pollFirst();

        if (hash == null) {
            // Кэш пуст - попытаемся получить хэши синхронно
            log.warn("Hash cache is empty, refilling synchronously");
            refillCacheSynchronously();
            hash = hashDeque.pollFirst();

            if (hash == null) {
                throw new RuntimeException("Unable to generate hash - database might be unavailable");
            }
        }

        checkAndRefillAsync();

        return hash;
    }

    @Async
    private void checkAndRefillAsync() {
        int currentSize = hashDeque.size();
        int threshold = (maxCacheSize * minThresholdPercent) / 100;

        if (currentSize < threshold) {
            log.debug("Hash cache level: {} (threshold: {}), refilling asynchronously",
                    currentSize, threshold);
            refillCacheAsync();
        }
    }

    private void refillCacheAsync() {
        executorService.submit(this::refillCacheSynchronously);
    }

    private void refillCacheSynchronously() {
        if (!refillSemaphore.tryAcquire()) {
            return;
        }

        try {
            int needed = maxCacheSize - hashDeque.size();
            if (needed <= 0) {
                return;
            }

            List<Hash> unusedHashes = addToHashDeque(needed);

            if (unusedHashes.size() < needed) {
                needed -= unusedHashes.size();
                // Асинхронно генерируем новые хэши для БД (в 10 раз больше)
                generateNewHashesAsync(needed * 10);
                addToHashDeque(needed);
            }

            log.warn("Refilled hash cache with {} hashes", unusedHashes.size());
        } finally {
            refillSemaphore.release();
        }
    }

    private List<Hash> addToHashDeque(int needed) {
        List<Hash> unusedHashes = hashRepository.findUnusedHashes(needed);

        for (Hash hash : unusedHashes) {
            hashDeque.addLast(hash.getHashValue());
        }
        return unusedHashes;
    }

    @Async
    private void generateNewHashesAsync(int count) {
        try {
            if (hashRepository.countUnusedHashes() <= maxCacheSize * 10L * minThresholdPercent / 100) {
                hashGenerator.generateAndSaveHashes(count);
            }
        } catch (Exception e) {
            log.error("Error generating new hashes", e);
        }
    }
}
