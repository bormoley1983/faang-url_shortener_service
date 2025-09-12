package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.UrlShortenerProperties;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.exception.HashGenerationException;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    @Qualifier("hashGeneratorExecutor")
    private final ExecutorService hashGeneratorExecutor;
    private final UrlShortenerProperties properties;

    private int getBatchSize() {
        return properties.getHashGenerator().getBatchSize();
    }

    /**
     * Асинхронно генерирует батч хэшей и сохраняет их в БД
     *
     * @return CompletableFuture с количеством сгенерированных хэшей
     */
    @Async("hashGeneratorExecutor")
    public CompletableFuture<Integer> generateBatch() {
        return CompletableFuture.supplyAsync(() -> {
            int batchSize = getBatchSize();
            log.info("Starting hash generation batch of size: {}", batchSize);

            try {
                List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(batchSize);
                log.debug("Generated {} unique numbers from sequence", uniqueNumbers.size());

                if (uniqueNumbers.isEmpty()) {
                    log.warn("No unique numbers generated from sequence");
                    return 0;
                }

                List<String> hashes = base62Encoder.encode(uniqueNumbers);
                log.debug("Encoded {} numbers to base62 hashes", hashes.size());

                if (hashes.isEmpty()) {
                    log.warn("No hashes generated from unique numbers");
                    return 0;
                }

                List<Hash> hashEntities = hashes.stream()
                        .map(hash -> Hash.builder()
                                .hash(hash)
                                .build())
                        .toList();

                List<Hash> savedHashes = hashRepository.saveAll(hashEntities);

                int savedCount = savedHashes.size();
                log.info("Successfully generated and saved {} hashes to database", savedCount);

                return savedCount;

            } catch (Exception e) {
                log.error("Failed to generate hash batch", e);
                throw new HashGenerationException("Hash generation failed", e);
            }
        }, hashGeneratorExecutor);
    }

    public void generateHashes() {
        generateBatch().thenAccept(count ->
                log.info("Completed hash generation, generated {} hashes", count)
        ).exceptionally(throwable -> {
            log.error("Hash generation failed", throwable);
            return null;
        });
    }
}