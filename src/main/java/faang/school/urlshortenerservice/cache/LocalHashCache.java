package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.config.property.HashProps;
import faang.school.urlshortenerservice.service.HashGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalHashCache {
    private final HashProps hashProps;
    private final HashGenerator hashGenerator;
    private final AtomicBoolean isFilling = new AtomicBoolean(false);
    private Queue<String> cache;

    @PostConstruct
    public void init() {
        cache = new ArrayBlockingQueue<>(hashProps.local().capacity());
        cache.addAll(hashGenerator.getHashes(hashProps.batchSize()));
        hashGenerator.generateBatchAsync();
    }

    public String getHash() {
        if (isBelowMinFill()) {
            if (isFilling.compareAndSet(false, true)) {
                hashGenerator.getHashesAsync(hashProps.batchSize())
                        .thenAccept(cache::addAll)
                        .whenComplete((res, ex) -> {
                            if (ex != null) {
                                log.error("Failed to get {} hashes. Cause: {}",
                                          hashProps.batchSize(), ex.getMessage(), ex);
                            }
                            isFilling.set(false);
                        });
            }
        }
        return cache.poll();
    }

    private boolean isBelowMinFill() {
        return (cache.size() * 100 / hashProps.local().capacity()) <= hashProps.local().minFillPercent();
    }
}
