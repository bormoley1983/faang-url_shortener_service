package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.generator.HashGenerator;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class LocalCache {
    private final HashGenerator hashGenerator;
    private final AtomicBoolean filling = new AtomicBoolean(false);
    @Value("${hash.capacity}")
    private int capacity;
    private Queue<String> hashLocal;
    @Value("${hash.min-capacity-percent}")
    private int minCapacityPercent;

    public LocalCache(HashGenerator hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    @PostConstruct
    public void init() {
        log.info("Starting filling queue");
        hashLocal = new ArrayBlockingQueue<>(capacity);
        hashLocal.addAll(hashGenerator.getHashes(capacity));
        log.info("Filling successful queue size {}", hashLocal.size());
    }

    public String getHash() {
        if (checkCapacity()) {
            if (!filling.compareAndSet(false, true)) {
                hashGenerator.getHashesAsync(capacity)
                        .thenAccept(hashLocal::addAll)
                        .thenRun(() -> filling.set(false));
            }
        }
        return hashLocal.poll();
    }

    private boolean checkCapacity() {
        return ((hashLocal.size() / (capacity / 100)) < minCapacityPercent);
    }

}