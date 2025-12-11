package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.encoder.Base62Encoder;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGeneratorImpl implements HashGenerator {
    private final Base62Encoder base62Encoder;
    private final HashRepository hashRepository;

    @Value("${hash.generator.batch-size}")
    private int batchSize;

    @Override
    @Async("hashGeneratorExecutor")
    public CompletableFuture<List<String>> generateBatch() {
        log.info("Starting hash batch generation with size: {}", batchSize);

        List<Long> range = hashRepository.getUniqueNumbers(batchSize);

        if (range.isEmpty()) {
            log.info("No unique numbers fetched from database");
            return CompletableFuture.completedFuture(List.of());
        }

        log.debug("Fetched {} unique numbers", range.size());

        List<String> hashes = base62Encoder.encode(range);
        hashRepository.save(hashes);
        log.info("Generated and saved {} hashes", hashes.size());
        return CompletableFuture.completedFuture(hashes);
    }
}