package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Сервис для генерации хэша для URL
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerate {

    private final Base62Encoder encoder;
    private final HashRepository repository;

    @Value("${app.batch.generated-hash}")
    private int batchUniqueNumber;

    @Async
    public void generateBatch() {
        try {
            List<Long> uniqueNumbers = repository.getUniqueNumbers(batchUniqueNumber);
            List<String> hashes = encoder.encode(uniqueNumbers);

            repository.saveAll(hashes);
            log.info("Успешная генерация и сохранения {} хэшей", hashes.size());
        } catch (Exception e) {
            log.error("Ошибка генерации хэш батча", e);
        }
    }

}