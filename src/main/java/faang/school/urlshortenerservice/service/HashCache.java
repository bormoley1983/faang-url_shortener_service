package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.config.HashConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class HashCache {
    private final HashConfig hashConfig;
    private final HashGenerator hashGenerator;
    private final HashRepository hashRepository;
    private final ThreadPoolTaskExecutor executor;
    private final BlockingQueue<String> hashQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean refilling = new AtomicBoolean(false);

    public HashCache(
            HashConfig hashConfig,
            HashGenerator hashGenerator,
            HashRepository hashRepository,
            @Qualifier("hashGeneratorExecutor") ThreadPoolTaskExecutor executor) {
        this.hashConfig = hashConfig;
        this.hashGenerator = hashGenerator;
        this.hashRepository = hashRepository;
        this.executor = executor;
    }

    @PostConstruct
    private void initCache() {
        refillCache();
    }

    public  String getHash() {
        try {
            String hash = hashQueue.poll(50, TimeUnit.MILLISECONDS);
            if (hash == null) {
                log.error("Cache refilling stopped.");
                throw new IllegalStateException("Failed to get hash from cache");
            }
            int left = hashQueue.size();
            log.debug("Getting hash from cache, left {}", left);

            startRefillIfNeeded();
            return hash;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for hash", e);
        }
    }

    public void refillCache() {
        List<String> hashes = hashGenerator.getFreeHashes(hashConfig.getCache().getSize());
        hashQueue.addAll(hashes);
        log.info("Updated cache with {} values, current size {}",
                hashConfig.getCache().getSize(), hashQueue.size());
    }

    private void startRefillIfNeeded() {
        int left = hashQueue.size();
        boolean needRefilling = left < hashConfig.getCacheUpdateCount() &&
                refilling.compareAndSet(false, true);

        if (needRefilling) {
            log.warn("Start refilling hash cache, current size: {}, limit: {}",
                    left, hashConfig.getCacheUpdateCount());
            refillCacheAsync();
        }
    }

    private void refillCacheAsync() {
        executor.submit(() -> {
            try {
                log.info("Start refilling hash cache.");
                refillCache();
            } finally {
                refilling.set(false);
            }
        });
    }

    public void returnHashes(List<String> hashes) {
        if (hashes.isEmpty()) {
            return;
        }

        log.info("Returning {} hashes to storage", hashes.size());

        for (String hash : hashes) {
            try {
                hashRepository.insertIntoFreeHashStorage(hash);
            } catch (Exception e) {
                log.warn("Failed to insert hash {} into free storage (maybe duplicate): {}",
                        hash, e.getMessage());
            }
        }
    }
}