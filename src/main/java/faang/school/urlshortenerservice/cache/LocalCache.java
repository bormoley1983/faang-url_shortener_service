package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.service.async.AsyncService;
import faang.school.urlshortenerservice.service.async.AsyncServiceImpl;
import faang.school.urlshortenerservice.service.hash.HashService;
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
    private static final int ONE_HUNDRED_PERCENT = 100;
    private final AsyncService asyncService;
    private final HashService hashService;
    private final AtomicBoolean filling = new AtomicBoolean(false);
    @Value("${hash.capacity}")
    private int capacity;
    private Queue<String> hashLocal;
    @Value("${hash.min-capacity-percent}")
    private int minCapacityPercent;

    public LocalCache(HashService hashService, AsyncServiceImpl asyncService) {
        this.hashService = hashService;
        this.asyncService = asyncService;
    }

    @PostConstruct
    public void init() {
        log.info("Starting filling queue");
        hashLocal = new ArrayBlockingQueue<>(capacity);
        hashLocal.addAll(hashService.getHashes(capacity));
        log.info("Filling successful queue size {}", hashLocal.size());
    }

    public String getHash() {
        if (checkCapacity()) {
            if (!filling.compareAndSet(false, true)) {
                asyncService.getHashesAsync(capacity)
                        .thenAccept(hashLocal::addAll)
                        .thenRun(() -> filling.set(false));
            }
        }
        return hashLocal.poll();
    }

    private boolean checkCapacity() {
        return ((hashLocal.size() / (capacity / ONE_HUNDRED_PERCENT)) < minCapacityPercent);
    }
}