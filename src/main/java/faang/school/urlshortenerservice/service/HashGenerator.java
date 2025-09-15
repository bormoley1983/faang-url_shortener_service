package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.property.HashProps;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGenerator {
    private final HashProps hashProps;
    private final Base62Encoder encoder;
    private final HashRepository hashRepository;

    @Async("hashTaskExecutor")
    public void generateBatchAsync() {
        generateBatch();
    }

    @Transactional
    public void generateBatch() {
        log.info("Starting generation hashes of {} size", hashProps.batchSize());
        List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(hashProps.batchSize());
        List<String> hashes = encoder.encode(uniqueNumbers);

        hashRepository.saveBatch(hashes.toArray(String[]::new));
    }

    @Async("hashTaskExecutor")
    public CompletableFuture<List<String>> getHashesAsync(int batchSize) {
        return CompletableFuture.completedFuture(getHashes(batchSize));
    }

    @Transactional
    public List<String> getHashes(int batchSize) {
        List<String> hashes = hashRepository.getHashBatch(batchSize);

        while (hashes.size() < batchSize) {
            log.info("Current hash amount in DB = {}, needed = {}", hashes.size(), batchSize);
            generateBatch();
            hashes.addAll(hashRepository.getHashBatch(batchSize - hashes.size()));
        }

        return hashes;
    }

    public void generateBatchIfNeeded() {
        if (isBelowMinFill()) {
            log.info("Current hash amount in DB < {}", hashProps.minStored());
            generateBatch();
        }
    }

    private boolean isBelowMinFill() {
        return hashRepository.count() <= hashProps.minStored();
    }
}
