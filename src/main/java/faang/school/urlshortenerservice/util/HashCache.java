package faang.school.urlshortenerservice.util;

import faang.school.urlshortenerservice.repository.HashRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

@Component
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
            if (cache.size() < bound) {
                hashCacheExecutor.execute(() -> cache.addAll(hashRepository.getHashBatch(batch)));
                hashGenerator.generateBatch();
            }
            return cache.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
