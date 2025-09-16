package faang.school.urlshortenerservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Конфигурация ExecutorService для асинхронного пополнения кэша хэшей.
 *
 * <p>Создаёт пул потоков фиксированного размера с ограниченной очередью задач,
 * используемый в HashCacheService для асинхронного refill.</p>
 */
@Configuration
public class ExecutorConfig {

    @Value("${hash.cache.executor.pool-size:2}")
    private int poolSize;

    @Value("${hash.cache.executor.queue-size:100}")
    private int queueSize;

    @Bean(name = "hashCacheExecutor")
    public ExecutorService hashCacheExecutor() {
        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueSize)
        );
    }
}