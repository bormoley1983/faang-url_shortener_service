package faang.school.urlshortenerservice.util;

import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    @Value("${hash.hash-generator.generate-batch}")
    private int batch;
    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        List<Long> numbers = hashRepository.getUniqueNumbers(batch);
        List<String> hashes = base62Encoder.encode(numbers);
        hashRepository.save(hashes);
    }
}
