package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class HashGenerator {
    @Value("${batch.size}")
    private int butchSize;

    private final Base62Encoder base62Encoder;
    private final HashRepository hashRepository;

    @Transactional
    @Scheduled(cron = "${scheduler.tasks.generate-batch.cron:0 0 2 * * *}")
    public void generateBatch() {
        List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(butchSize);
        List<Hash> hashes = base62Encoder.encode(uniqueNumbers);
        hashRepository.saveAll(hashes);
    }

    @Transactional
    @Async("hashGeneratorExecutor")
    public CompletableFuture<List<Hash>> getHashes(int amount) {
        List<Hash> hashes = hashRepository.getHashBatch(amount);
        if (hashes.size() < amount) {
            generateBatch();
            hashes.addAll(getHashes(amount - hashes.size()).join());
        }
        return CompletableFuture.completedFuture(hashes);
    }
}