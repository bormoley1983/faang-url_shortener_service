package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис для генерации хэша для URL
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final Base62Encoder encoder;
    private final HashRepository repository;

    @Value("${app.batch.generated-hash}")
    private int batchUniqueNumber;

    @Value("${app.redis.database.keys.local-cache:local_cache}")
    private String localCacheKey;

    @Transactional
    public void generateBatch() {
        List<Long> uniqueNumbers = repository.getUniqueNumbers(batchUniqueNumber);
        List<String> hashes = encoder.encode(uniqueNumbers);

        repository.saveAll(hashes);
        log.info("Успешная генерация и сохранения {} хэшей", hashes.size());
    }

    @Transactional
    public List<String> getHashes(long amount) {
        List<String> hashes = repository.findAndDelete(amount);
        if (hashes.size() < amount) {
            generateBatch();
            hashes.addAll(repository.findAndDelete(amount - hashes.size()));
        }
        return hashes;
    }

    @Async("hashGenerateExecutor")
    public CompletableFuture<List<String>> getHashesAsync(long amount) {
        return CompletableFuture.completedFuture(getHashes(amount));
    }
}