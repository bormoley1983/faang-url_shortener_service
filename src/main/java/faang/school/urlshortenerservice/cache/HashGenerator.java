package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.repository.UniqueNumbersRepository;
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

    private final UniqueNumbersRepository repository;
    private final Base62Encoder encoder;
    @Value("${spring.jpa.hibernate.seq_batch_size}")
    private Long seqBatchSize;

    @Transactional
    public List<String> generateHashes() {
        List<Long> uniqueNumbers = repository.getUniqueNumbers(seqBatchSize);
        return encoder.encodeNumbers(uniqueNumbers);
    }
}
