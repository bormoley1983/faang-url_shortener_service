package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author agent
 * @since 12.09.2025
 */
@Service
@RequiredArgsConstructor
public class HashGeneratorService {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    /**
     * Асинхронная генерация новой пачки хэшей.
     * Количество чисел берётся из конфига.
     */
    @Async("hashGeneratorExecutor")
    public void generateBatch(int batchSize) {
        List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);

        List<String> hashes = base62Encoder.encode(numbers);

        hashRepository.saveBatch(hashes);
    }
}