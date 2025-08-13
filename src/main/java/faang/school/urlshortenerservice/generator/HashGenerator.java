package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.hash.batch-size}")
    private int batchSize;

    @Async("hashGeneratorExecutor")
    @Transactional
    public void generateBatch() {

        List<Long> uniqueNumbers = hashRepository.getUniqueNumbers(batchSize);

        if (uniqueNumbers.isEmpty()) {
            return;
        }

        List<String> hashes = base62Encoder.encode(uniqueNumbers);

        List<Hash> entities = hashes.stream()
                .map(Hash::new)
                .toList();

        hashRepository.saveAll(entities);
    }
}