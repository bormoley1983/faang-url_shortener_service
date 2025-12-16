package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
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

    @Value("${hash.storage.max-size}")
    private int capacity;

    @Value("${hash.storage.threshold}")
    private int threshold;

    @Value("${hash.storage.hash-range:100}")
    private int hashRange;

    private LinkedBlockingQueue<Hash> cachedHashesQueue;

    private final ExecutorService executorService;
    private final HashRepository hashRepository;
    private final LocalCache localCache;
    private final HashGenerator hashGenerator;
    private final AtomicBoolean isLoadingHashCash = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        cachedHashesQueue = new LinkedBlockingQueue<>(capacity);
    }

    public Hash getHash() {
        Hash hash = cachedHashesQueue.poll();
        int size = cachedHashesQueue.size();
        int maxSize = cachedHashesQueue.remainingCapacity() + size;

        if (size <= threshold * maxSize) {
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
                List<Hash> hashes = localCache.getHashes(hashRange);
                for (Hash hash : hashes) {
                    cachedHashesQueue.offer(hash);
                }
            } finally {
                isLoadingHashCash.set(false);
            }
        });
    }
}
