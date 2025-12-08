package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.base62encoder.Base62Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    @Value("${url-shortener.hash-generator.batch-size:100}")
    private int batchSize;

    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        log.info("HashGenerator started batch generation");

        List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);

        List<String> hashes = base62Encoder.encode(numbers);

        List<Hash> entities = hashes.stream()
                .map(h -> {
                    Hash hash = new Hash();
                    hash.setHash(h);
                    return hash;
                })
                .toList();

        hashRepository.saveAll(entities);

        log.info("HashGenerator finished batch generation, generated {} hashes", hashes.size());
    }
}
