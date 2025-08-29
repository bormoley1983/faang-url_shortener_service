package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.encoder.Base62Encoder;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class HashService {

    private final Base62Encoder base62Encoder;
    private final HashRepository hashRepository;
    private final Executor hashCacheExecutor;
    private final Executor hashGeneratorExecutor;

    public HashService(
            Base62Encoder base62Encoder,
            HashRepository hashRepository,
            @Qualifier("hashCacheExecutor") Executor hashCacheExecutor,
            @Qualifier("hashGeneratorExecutor") Executor hashGeneratorExecutor) {
        this.base62Encoder = base62Encoder;
        this.hashRepository = hashRepository;
        this.hashCacheExecutor = hashCacheExecutor;
        this.hashGeneratorExecutor = hashGeneratorExecutor;
    }

    @Value(value = "${app.hash.table-size}")
    private long tableSize;
    @Value(value = "${app.hash.memory-cache-min-percentage}")
    private int minimumFillPercentage;
    @Value(value = "${app.hash.batch-size}")
    private int batchSize;
    @Value(value = "${app.hash.lock-id}")
    private int lockId;

    @Transactional
    public void getHashes() {
        ();
    }

    private List<String> generateHashes() {
        return null;
    }

    private void generateHashBatches() {
        if (hashRepository.tryLock(lockId)) {
            long currentHashCount = hashRepository.count();
            if (checkCurrentFillPercentage(currentHashCount)) {
                generateHashBatchAsync(tableSize - currentHashCount)
                        .thenAccept(hashes -> {
                            List<Hash> savedHashes = hashRepository.saveAll(hashes);
                        })
                        .whenComplete((result, exception) -> {
                            hashRepository.unlock(lockId);
                            if (exception != null) {
                                log.error("Error occurred during cache generation", exception);
                            }
                        });
            }
        }
    }


    private List<Hash> encodeBatch(List<Long> batchList) {
        return batchList
                .stream()
                .map(base62Encoder::encode)
                .map(Hash::new)
                .toList();
    }

    private CompletableFuture<List<Hash>> generateHashBatchAsync(long batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<Long> batchList = hashRepository.getUniqueSequenceValues(batchSize);
            return encodeBatch(batchList);
        }, hashGeneratorExecutor);
    }

    private boolean checkCurrentFillPercentage(long currentHashCount) {
        return (currentHashCount * 100 / tableSize) <= minimumFillPercentage;
    }
}
