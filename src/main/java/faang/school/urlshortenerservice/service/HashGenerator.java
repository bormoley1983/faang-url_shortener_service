package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepositoryJdbc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashGenerator {
    @Value("${hash-generator.batch.size}")
    private int batchSize;

    private final Base62Encoder base62Encoder;
    private final HashRepositoryJdbc hashRepository;

    @Transactional
    @Scheduled(cron = "${scheduler.tasks.generate-batch.cron:0 0 0 * * *}")
    public void generateBatch() {
        List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(batchSize);
        List<Hash> hashes = base62Encoder.encode(uniqueNumbers).stream()
                .map(Hash::new)
                .toList();

        int amount = hashRepository.saveAll(hashes);
        log.info("Generated {} hashes", amount);
    }

    @Transactional
    public List<String> getHashes(int amount) {
        List<Hash> hashes = hashRepository.getHashBatch(amount);
        if (hashes.size() < amount) {
            generateBatch();
            hashes.addAll(hashRepository.getHashBatch(amount - hashes.size()));
        }
        return hashes.stream().map(Hash::getHash).toList();
    }

    @Async("hashGeneratorExecutor")
    public CompletableFuture<List<String>> getHashesAsync(int amount) {
        return CompletableFuture.completedFuture(getHashes(amount));
    }
}