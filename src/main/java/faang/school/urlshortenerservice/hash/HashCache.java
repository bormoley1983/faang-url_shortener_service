package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class HashCache {

    private LinkedBlockingQueue<Hash> cachedHashesQueue;

    private final HashStorageProperties properties;
    private final ExecutorService executorService;
    private final LocalCache localCache;
    private final AtomicBoolean isLoadingHashCash = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        cachedHashesQueue = new LinkedBlockingQueue<>(properties.getMaxSizeCachedQueue());
    }

    public Hash getHash() {
        Hash hash = cachedHashesQueue.poll();
        int size = cachedHashesQueue.size();
        int maxSize = cachedHashesQueue.remainingCapacity() + size;

        if (size <= properties.getThresholdToRefillCachedQueue() * maxSize) {
            checkHashLoading();
        }
        return hash;
    }

    private void checkHashLoading() {
        if (!isLoadingHashCash.compareAndSet(false, true)) {
            return;
        }
        executorService.submit(() -> {
            try {
                List<Hash> hashes = localCache.getHashes(properties.getHashRange());
                for (Hash hash : hashes) {
                    put(hash);
                }
            } finally {
                isLoadingHashCash.set(false);
            }
        });
    }

    public void put(Hash hash) {
        cachedHashesQueue.offer(hash);
    }
}
