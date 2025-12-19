package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.commons.collections4.ListUtils.partition;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {

    private static final Integer POOL_SIZE_THREADS = 1500;

    @Value("${hash.generator.batch-size.save-bd:500}")
    private Integer batchSizeForSaveBd;

    @Value("${hash.local.count.hash:1000}")
    private Integer numberOfLocalHash;

    @Value("${hash.generator.max-range:10000000}")
    private Integer maxRange;

    private final HashRepository hashRepository;
    private final Base62Encode base62Encode;
    private final SaveBdService saveBdService;

    private final AtomicBoolean isCheckingSizeInBd = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE_THREADS);

    @Transactional
    public List<Hash> getHash() {

        List<Hash> hashes = hashRepository.deleteAndReturnFirstN(numberOfLocalHash);
        log.info("generate hash for local hash! size - {}", hashes.size());

        return hashes;
    }

    @Transactional
    public void hashGenerator() {
        hashGenerator(maxRange);
    }

    @Transactional
    public void hashGenerator(int count) {
        List<Long> listNumbers = hashRepository.getNextRange(count);

        log.info("Generating {} hashes", listNumbers.size());

        List<Hash> hashes = base62Encode.generateHashByBase62(listNumbers);
        List<Hash> mutable = new ArrayList<>(hashes);
        Collections.shuffle(mutable, ThreadLocalRandom.current());

        saveHashesInBatches(mutable);
    }

    public void saveHashesInBatches(List<Hash> hashes) {

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        if (isCheckingSizeInBd.compareAndSet(false, true)) {
            List<List<Hash>> batches = partition(hashes, batchSizeForSaveBd);
            Long timeStart = System.currentTimeMillis();

            for (List<Hash> batch : batches) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> saveBdService.saveSingleBatch(batch),
                                executorService)
                        .thenRun(() -> {
                            log.info("Total batch insert time: {} ms for {} records ",
                                    System.currentTimeMillis() - timeStart, batch.size());
                        })
                        .exceptionally(ex -> {
                            log.error("Failed to save batch", ex);
                            return null;
                        });
                futures.add(future);
            }
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allFutures.join();
            isCheckingSizeInBd.set(false);
        }
    }
}
