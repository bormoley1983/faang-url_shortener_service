package faang.school.urlshortenerservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashCache {

    @Value("${base.chars}")
    private String baseChars;


    @Value("${app.hash.cache.size:100}")
    private int cacheSize;

    @Value("${app.hash.cache.threshold-percent:20}")
    private int thresholdPercent;

    private final ArrayBlockingQueue<String> hashQueue = new ArrayBlockingQueue<>(cacheSize);

    private long currentId = 1;

    private final AtomicBoolean generationInProgress = new AtomicBoolean(false);


    @PostConstruct
    public void init() {
        log.info("Инициализация HashCache с размером очереди: {}", cacheSize);
        replenishQueue();
    }

    public String getHash() {
        if (isBelowThreshold()) {
            generateBatchAsync();
        }
        String hash = hashQueue.poll();
        if (hash == null) {
            log.warn("Очередь пуста, генерируем фоллбэк-хэш");
            return fallbackHash();
        }
        return hash;
    }

    private boolean isBelowThreshold() {
        int threshold = cacheSize * thresholdPercent / 100;
        return hashQueue.size() < threshold;
    }

    @Async("hashGeneratorExecutor")
    public void generateBatchAsync() {
        if (!generationInProgress.compareAndSet(false, true)) {
            log.warn("Пополнение уже запущено. Пропускаем.");
            return;
        }

        try {
            int needed = cacheSize - hashQueue.size();
            List<String> newHashes = new ArrayList<>(needed);

            for (int i = 0; i < needed; i++) {
                newHashes.add(encode(currentId++));
            }

            int added = 0;
            for (String hash : newHashes) {
                if (hashQueue.offer(hash)) {
                    added++;
                } else {
                    break;
                }
            }
            log.info("Асинхронно добавлено {} хэшей", added);

        } catch (Exception e) {
            log.error("Ошибка при асинхронном пополнении хэшей", e);
        } finally {
            generationInProgress.set(false);
        }
    }

    private void replenishQueue() {
        List<String> initialBatch = new ArrayList<>();
        for (int i = 0; i < cacheSize; i++) {
            initialBatch.add(encode(currentId++));
        }
        hashQueue.addAll(initialBatch);
        log.info("Очередь инициализирована: {} хэшей", initialBatch.size());
    }

    private String encode(long num) {
        if (num == 0) {
            return String.valueOf(baseChars.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        int base = baseChars.length();
        while (num > 0) {
            int remainder = (int) (num % base);
            sb.append(baseChars.charAt(remainder));
            num /= base;
        }
        return sb.reverse().toString();
    }

    private String fallbackHash() {
        return "x" + System.currentTimeMillis() % 100000;
    }
}