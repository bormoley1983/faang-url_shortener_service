package faang.school.urlshortenerservice.generator;

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

    @Value("${hash.cache.capacity:10000}")
    private int cacheCapacity;

    private AtomicBoolean filling;

    private final Queue<String> hashes = new ArrayBlockingQueue<>(cacheCapacity);

    @PostConstruct
    public void init() {
        hashes.addAll(hashGenerator.getHashes(cacheCapacity));
    }

    public String getHash() {
        if (hashes.size() / (cacheCapacity / 100) < 20) { // 2000 / 100 < 0.2
            if(filling.compareAndSet(false, true)) {
                hashGenerator.getHashesAsync(cacheCapacity)
                        .thenAccept(hashes::addAll)
                        .thenRun(() -> {filling.set(false);});

            }

        }
        return hashes.poll();
    }
}
