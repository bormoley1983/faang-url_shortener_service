package faang.school.urlshortenerservice.cache;

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
 * Когда размер очереди уменьшается до 20%, асинхронно запускается генерация нового батча хэшей
 * в случае, если другой поток уже этого не сделал
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HashCache {

    private final AtomicBoolean isRefilling = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<String> hashes = new ConcurrentLinkedQueue<>();
    @Value("${spring.jpa.hibernate.queue_size}")
    private Long queueSize;
    private final HashGenerator hashGenerator;

    public String getHash() {
        if (hashes.size() <= queueSize / 5 && isRefilling.compareAndSet(false, true)) {
            CompletableFuture.runAsync(this::generateAndGetHashesBatch);
        }

        if (hashes.isEmpty()) { throw new RuntimeException("Нет доступных хэшей"); }

        return hashes.poll();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void generateAndGetHashesBatch() {
        try {
            log.info("Запущена генерация новых хэшей");
            List<String> generatedHashes = hashGenerator.generateHashes();
            hashes.addAll(generatedHashes);
        } finally {
            isRefilling.set(false);
        }
    }
}
