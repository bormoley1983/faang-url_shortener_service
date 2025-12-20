package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.encoder.BaseEncoder;
import faang.school.urlshortenerservice.properties.HashBatchProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGeneratorImpl implements HashGenerator {

    private final HashRepository hashRepository;
    private final BaseEncoder baseEncoder;
    private final HashBatchProperties hashBatchProperties;

    @Async("hashGeneratorThreadPool")
    @Override
    public CompletableFuture<Void> generateBatch() {
        log.info("Starting hash generation batch");

        try {
            Integer generationSize = hashBatchProperties.getHashGenerationSize();
            List<Long> numbers = hashRepository.getUniqueNumbers(generationSize);
            log.debug("Received {} unique numbers from sequence", numbers.size());

            List<String> hashes = baseEncoder.encode(numbers);
            log.debug("Encoded {} numbers to Base62 hashes", hashes.size());

            hashRepository.save(hashes);
            log.info("Successfully generated and saved {} hashes", hashes.size());

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to generate hash batch", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
