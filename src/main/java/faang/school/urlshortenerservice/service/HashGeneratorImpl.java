package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.hash.HashGeneratorAsyncConfig;
import faang.school.urlshortenerservice.config.hash.HashProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashGeneratorImpl implements HashGenerator {

    private final HashRepository hashRepository;
    private final HashProperties hashProperties;
    private final Base62Encoder base62Encoder;

    @Override
    @Async(HashGeneratorAsyncConfig.HASH_GENERATOR_EXECUTOR)
    public CompletableFuture<Integer> generateBatch() {
        int batchSize = hashProperties.getBatchSize();
        List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);
        if (numbers.size() != batchSize) {
            log.warn("Expected {} numbers, got {}", batchSize, numbers.size());
        }
        List<String> hashes = base62Encoder.encode(numbers);
        if (hashes.size() != numbers.size()) {
            throw new IllegalStateException("Encoder returned size mismatch");
        }
        hashRepository.save(hashes);
        log.info("Generated and saved {} hashes", hashes.size());
        return CompletableFuture.completedFuture(hashes.size());
    }
}
