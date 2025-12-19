package faang.school.urlshortenerservice.util;

import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class HashCache {

    @Value("${hash.hash-cache.get-batch}")
    private int batch;
    @Value("${hash.hash-cache.limit}")
    private int limit;
    @Value("${hash.hash-cache.bound}")
    private int bound;
    private final HashGenerator hashGenerator;
    private final HashRepository hashRepository;
    private final ExecutorService hashCacheExecutor;
    private final LinkedBlockingQueue<String> cache = new LinkedBlockingQueue<>(limit);
    private final AtomicBoolean isGenerating = new AtomicBoolean(false);

    public HashCache(
            HashGenerator hashGenerator,
            HashRepository hashRepository,
            @Qualifier("hashCacheExecutor") ExecutorService hashCacheExecutor) {
        this.hashGenerator = hashGenerator;
        this.hashRepository = hashRepository;
        this.hashCacheExecutor = hashCacheExecutor;
    }

    public String getHash() {
        try {
            if (cache.size() < bound && isGenerating.compareAndSet(false, true)) {
                log.info("The size of the cache is less than 20%");
                CompletableFuture.runAsync(() -> {
                            cache.addAll(hashRepository.getHashBatch(batch));
                            hashGenerator.generateBatch();
                        }, hashCacheExecutor)
                        .whenComplete((result, ex) -> isGenerating.set(false))
                        .exceptionally(ex -> {
                            throw new RuntimeException(ex);
                        });
            }
            return cache.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
