package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

    private LinkedBlockingQueue<Hash> cachedHashesQueue;

    private final ExecutorService executorService;
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final AtomicBoolean isLoadingHashCash = new AtomicBoolean(false);

    @Value("${hash.storage.hash-range:1000}")
    private int hashRange = 1000; //TODO положить в application

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
        if (hash == null) {
            Optional<Hash> oneHash = hashRepository.getAndDeleteOne();
            hash = oneHash.map(Hash::getHash).orElseThrow(() -> new IllegalStateException("No hashes available in DB"));
        }
        return hash;
    }

    private void checkHashLoading() {
        if (!isLoadingHashCash.compareAndSet(false, true)) {
            return;
        }
        executorService.submit(() -> {
            try {
                List<Hash> hashes = hashGenerator.getHashes();
                for (Hash hash : hashes) {
                    cachedHashesQueue.offer(hash.toString());
                }
            } finally {
                isLoadingHashCash.set(false);
            }
        });
    }
}
