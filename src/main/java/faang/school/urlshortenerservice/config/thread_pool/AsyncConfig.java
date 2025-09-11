package faang.school.urlshortenerservice.config.thread_pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Конфигурация для создания собственного пула потоков
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Component
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor generateHash(
            @Value("${app.async.postgres.core-pool-size}") int corePoolSize,
            @Value("${app.async.postgres.max-pool-size}") int maxPoolSize,
            @Value("${app.async.postgres.queue-capacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(queueCapacity);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);

        executor.setThreadNamePrefix("Generate-Hash-Async-");
        executor.initialize();

        return executor;
    }
}