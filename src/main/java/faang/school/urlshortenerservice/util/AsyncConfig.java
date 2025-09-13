package faang.school.urlshortenerservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Конфигурация асинхронного исполнителя для генерации хэшей.
 *
 * <p>Создаёт ThreadPoolTaskExecutor с настраиваемым размером пула и очереди,
 * используемый сервисом HashCacheService для асинхронного пополнения кэша хэшей.</p>
 */
@Configuration
public class AsyncConfig {

    @Value("${hash.generator.executor.pool-size:2}")
    private int poolSize;

    @Value("${hash.generator.executor.queue-size:100}")
    private int queueSize;

    @Bean(name = "hashGeneratorExecutor")
    public Executor hashGeneratorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("hash-generator-");
        executor.initialize();
        return executor;
    }
}