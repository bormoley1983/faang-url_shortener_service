package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Класс для получения уникального хэша. В качестве хранения батча хэшей использует потокобезопасную очередь.
 * Когда размер очереди уменьшается до 20%, асинхронно запускается получение из БД и генерация нового батча хэшей
 * в случае, если другой поток уже этого не сделал
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private static final AtomicBoolean isRefilling = new AtomicBoolean(false);
    private static ConcurrentLinkedQueue<String> hashes = new ConcurrentLinkedQueue<>();
    private static Long queueSize = 100L;
    private final HashGenerator hashGenerator;
    private final HashRepository hashRepository;
    @Value("${spring.jpa.hibernate.batch_size}")
    private Long batchSize;

    @Transactional
    public String getHash() {
        if (hashes.size() < queueSize / 5 && isRefilling.compareAndSet(false, true)) {
            CompletableFuture.runAsync(this::generateAndGetHashesBatch);
        }
        return hashes.poll();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void generateAndGetHashesBatch() {
        try {
            log.info("Запущено асинхронное получение батча хэшей из бд и генерация новых");
            hashGenerator.generateHashes();
            List<String> generatedHashes = hashRepository.getHashesBatch(batchSize);
            hashes.addAll(generatedHashes);
            hashRepository.deleteAllByIdInBatch(generatedHashes);
            queueSize = (long) hashes.size();
        } finally {
            isRefilling.set(false);
        }
    }
}
