package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * Класс для получения батча уникальных sequence, передачи их в класс для кодирования и сохранения
 * полученных значений уникальных хэшей в таблицу hash батчем
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository repository;
    private final Base62Encoder encoder;
    @Value("${spring.jpa.hibernate.batch_size}")
    private Long batchSize;

    @Transactional
    public void generateHashes() {
        List<Long> uniqueNumbers = repository.getUniqueNumbers(batchSize);
        log.info("Получен массив уникальных sequence");
        List<String> stringHashes = encoder.encodeNumbers(uniqueNumbers);
        log.info("Получен массив уникальных хэшей");
        List<Hash> hashes = stringHashes.stream()
                .map(Hash::new)
                .toList();
        repository.saveAll(hashes);
        log.info("Успешное сохранение хэшей в БД");
    }
}
