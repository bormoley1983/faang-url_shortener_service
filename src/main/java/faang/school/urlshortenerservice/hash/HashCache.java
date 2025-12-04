package faang.school.urlshortenerservice.hash;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class HashCache {
    private final HashGenerator hashGenerator;

    @Value("${hash.cache.capacity}")
    private int cacheCapacity;

    @Value("${hash.cache.min_percent_filling}")
    private int minPercentFillingCache;

    private final AtomicBoolean isCacheFillingInProgress = new AtomicBoolean(false);

    private final Queue<String> hashes = new ArrayBlockingQueue<>(cacheCapacity);

    @PostConstruct
    public void fillCache() {
        hashes.addAll(hashGenerator.getHashes(cacheCapacity));
    }

    public String getHash() {
        if (countCurrentPercentFillingCache() < minPercentFillingCache) {
            if (isCacheFillingInProgress.compareAndSet(false, true)) {
                hashGenerator.getHashesAsync(cacheCapacity)
                        .thenAccept(hashes::addAll)
                        .thenRun(() -> isCacheFillingInProgress.set(false));
            }
        }
        return hashes.poll();
    }
    
    private double countCurrentPercentFillingCache() {
        return 100 / (double) cacheCapacity * hashes.size();
    }
}
