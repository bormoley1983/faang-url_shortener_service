package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сервис кэширования коротких хэшей.
 *
 * <p>
 * Сервис предоставляет хэши для последующего сокращение из кэша
 * Имеет автоматическое ассинхронное пополнение
 * </p>
 *
 * <p>
 * Основные функции:
 * <ul>
 *     <li>getHash() — получение одного хэша из кэша.</li>
 *     <li>Автопополнение кэша уровни меньше 20%.</li>
 *     <li>Создание первичного набора хэшей после старта приложения.</li>
 * </ul>
 * </p>
 *
 * @author andreyfomchenko
 * @since 18.09.2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final HashRepository hashRepository;
    private final HashGeneratorService service;
    private final ExecutorService executor;

    @Value("${hash.cache.max-size:200}")
    private int maxCacheSize;

    @Value("${hash.cache.refill-threshold-percent:20}")
    private int replenishmentLevel;

    @Value("${hash.generator.batch-size:10}")
    private int batchSize;

    private final Queue<String> queue = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        log.info("Начало работы HashGenerator, начинаю пополнение кэша");
        asyncCompletion();
    }

    public String getHash() {
        String hash = queue.poll();
        log.info("Пополняем кэш, текущий размер кэша: {}", queue.size());
        if (hash == null || queue.size() * 100 / maxCacheSize < replenishmentLevel) {
            log.info("Размер кэша ниже порога ({}%), запускаем refill", replenishmentLevel);
            asyncCompletion();
        }
        return hash;
    }


    public void asyncCompletion() {
        if (isRefilling.compareAndSet(false, true)) {
            executor.submit(this::completionCache);
        } else {
            log.info("Поток занят");
        }
    }

        public void completionCache () {
            try {
                log.info("Начинаем заполнение хешей, сейчас в кэше {} ", queue.size());
                List<String> hashes = hashRepository.getHashBatch(batchSize);
                if (hashes.isEmpty()) {
                    log.warn("Не удалось получить хэши из базы! Генерируем новые...");
                    service.generateHashes(batchSize);
                    hashes = hashRepository.getHashBatch(batchSize);
                    if (hashes.isEmpty()) {
                        log.error("Не удалось получить хэши даже после генерации!");
                        return;
                    }
                }
                queue.addAll(hashes);
                log.info("После пополнение кеша: Добвлено {} хешей, текущий размер кеша {} ", hashes.size()
                                                                                            , queue.size());
                service.generateHashes(batchSize);
            } catch (Exception e) {
                log.error("Ошибка при заполнении кэша: {}", e.getMessage(), e);
                throw new RuntimeException("Ошибка заполнения кэша хэшей", e);
            } finally {
                isRefilling.set(false);
            }
        }
    }


