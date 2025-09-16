package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сервис кэширования коротких хэшей.
 *
 * <p>
 * Предоставляет хэши для URL-сокращателя из внутреннего кэша.
 * Автоматически пополняет кэш асинхронно при достижении порога.
 * </p>
 *
 * <p>
 * Основные функции:
 * <ul>
 *     <li>getHash() — получение одного хэша из кэша.</li>
 *     <li>Автопополнение кэша при падении ниже порога.</li>
 *     <li>Инициализация первичного набора хэшей после старта приложения.</li>
 * </ul>
 * </p>
 *
 * @author agent
 * @since 12.09.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HashCacheService {

    private final HashRepository hashRepository;
    private final HashGeneratorService hashGeneratorService;
    private final ExecutorService hashCacheExecutor;

    private final Queue<String> cache = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @Value("${hash.cache.max-size:1000}")
    private int maxCacheSize;

    @Value("${hash.cache.refill-threshold-percent:20}")
    private int refillThresholdPercent;

    @Value("${hash.generator.batch-size:100}")
    private int batchSize;

    @PostConstruct
    public void init() {
        log.info("Инициализация HashCacheService, запуск первичного refill");
        triggerRefill();
    }

    /**
     * Получает один хэш из кэша.
     * Если кэш ниже порога — асинхронно пополняет.
     */
    public String getHash() {
        String hash = cache.poll();
        log.info("getHash() вызван, текущий размер кэша: {}", cache.size());

        if (hash == null || cache.size() * 100 / maxCacheSize < refillThresholdPercent) {
            log.info("Размер кэша ниже порога ({}%), запускаем refill", refillThresholdPercent);
            triggerRefill();
        }

        return hash;
    }

    /**
     * Запускает асинхронное пополнение кэша.
     */
    public void triggerRefill() {
        if (isRefilling.compareAndSet(false, true)) {
            hashCacheExecutor.submit(this::refillCache);
        } else {
            log.info("Refill уже выполняется другим потоком");
        }
    }

    /**
     * Реальная логика пополнения кэша.
     */
    private void refillCache() {
        try {
            log.info("Пополнение кэша хэшей...");
            List<String> hashes = hashRepository.getHashBatch(batchSize);
            cache.addAll(hashes);
            log.info("Добавлено {} хэшей в кэш, текущий размер: {}", hashes.size(), cache.size());

            hashGeneratorService.generateBatch(batchSize);
        } finally {
            isRefilling.set(false);
            log.info("Refill завершён, isRefilling сброшен");
        }
    }
}