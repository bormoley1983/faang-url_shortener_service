package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import faang.school.urlshortenerservice.service.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    @Value("${app.hash.batch-size}")
    private int batchSize;

    @Transactional
    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        log.info("Начало генерации батча хэшей: размер = {}", batchSize);

        try {
            List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);
            log.debug("Получено {} уникальных чисел из sequence", numbers.size());

            List<String> hashes = base62Encoder.encode(numbers);
            log.debug("Закодировано {} хэшей", hashes.size());

            List<Hash> entities = hashes.stream()
                    .map(h -> Hash.builder().hashValue(h).build())
                    .toList();

            hashRepository.saveAll(entities);

            log.info("Успешно сгенерировано и сохранено {} хэшей", entities.size());
        } catch (Exception e) {
            log.error("Ошибка при генерации батча хэшей", e);
            throw e;
        }
    }

    @Scheduled(cron = "${app.hash.generator.cron:0 0 4,8,12 * * *}")
    public void scheduledGenerate() {
        generateBatch();
    }
}