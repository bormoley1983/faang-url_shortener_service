package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.service.config.HashConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashCache {
    private final HashConfig hashConfig;
    private final HashService hashService;
    private final AtomicInteger cacheCounter = new AtomicInteger();

    @Qualifier("hashGeneratorExecutor")
    private final ThreadPoolTaskExecutor executor;

    private final BlockingQueue<String> hashQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean refilling = new AtomicBoolean(false);

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final Object initLock = new Object();

    public String getHash() {
        ensureInitialized();

        try {
            String hash = hashQueue.poll(30, TimeUnit.SECONDS);
            if (hash == null) {
                log.error("Cache refilling stopped.");
                throw new IllegalStateException("Failed to get hash from cache");
            }

            if (initialized.get() && hashQueue.isEmpty()) {
                log.warn("Cache is empty but was initialized, trying to refill...");
                refillCache();
                hash = hashQueue.poll(5, TimeUnit.SECONDS);
            }
            if (hash == null) {
                throw new IllegalStateException("Failed to get hash from cache");
            }

            int left = cacheCounter.decrementAndGet();
            log.debug("Getting hash from cache, left {}", left);

            startRefillIfNeeded();
            return hash;
        } catch (InterruptedException e) {
            log.error("Failed to get unused hash");
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for hash", e);
        }
    }

    public void refillCache() {
        List<String> hashes = hashService.getFreeHashes(hashConfig.getCache().getSize());
        hashQueue.addAll(hashes);
        cacheCounter.set(hashQueue.size());
        log.info("Updated cache with {} values, current size {}", hashConfig.getCache().getSize(), hashQueue.size());
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initOnContextRefreshed() {
        log.info("=== INITIALIZING HashCache ===");
        log.info("HashConfig cache size: {}", hashConfig.getCache().getSize());
        log.info("HashService: {}", hashService);

        try {
            initializeCache();
            log.info("=== HashCache INITIALIZED. Size: {} ===", hashQueue.size());
        } catch (Exception e) {
            log.error("=== HashCache INITIALIZATION FAILED ===", e);
        }
    }

    private void ensureInitialized() {
        if (!initialized.get()) {
            synchronized (initLock) {
                if (!initialized.get()) {
                    log.info("Lazy initializing HashCache on first request...");
                    initializeCache();
                }
            }
        }
    }

    private void initializeCache() {
        try {
            refillCache();
            initialized.set(true);
            log.info("HashCache initialized successfully. Size: {}", hashQueue.size());
        } catch (Exception e) {
            log.error("Failed to initialize HashCache: {}", e.getMessage());
        }
    }

    private void startRefillIfNeeded() {
        int left = cacheCounter.get();
        boolean needRefilling = left < hashConfig.getCacheUpdateCount() &&
                refilling.compareAndSet(false, true);

        if (needRefilling) {
            log.warn("Start refilling hash cache, current size: {}, limit: {}", left, hashConfig.getCacheUpdateCount());
            refillCacheAsync();
        }
    }

    private void refillCacheAsync() {
        if (executor == null || executor.getThreadPoolExecutor().isShutdown()) {
            log.warn("Executor not available for async refill");
            refilling.set(false);
            return;
        }

        executor.execute(() -> {
            try {
                log.info("Start refilling hash cache.");
                refillCache();
            } catch (Exception e) {
                log.error("Error during async cache refill: {}", e.getMessage());
            } finally {
                refilling.set(false);
            }
        });
    }
}
