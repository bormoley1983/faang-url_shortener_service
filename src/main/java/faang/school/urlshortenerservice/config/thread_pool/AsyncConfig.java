package faang.school.urlshortenerservice.config.thread_pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Конфигурация для создания собственного пула потоков
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("hashGenerateExecutor")
    public ThreadPoolTaskExecutor generateHash(
            @Value("${app.thread-pool.async.generated-hash.core-pool-size}") int corePoolSize,
            @Value("${app.thread-pool.async.generated-hash.max-pool-size}") int maxPoolSize,
            @Value("${app.thread-pool.async.generated-hash.queue-capacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(queueCapacity);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);

        executor.setThreadNamePrefix("Generate-Hash-Async-");
        executor.initialize();
        return executor;
    }

    @Bean("afterCommitExecutor")
    public ThreadPoolTaskExecutor afterCommitExecutor(
        @Value("${app.thread-pool.async.generated-hash.core-pool-size}") int corePoolSize,
        @Value("${app.thread-pool.async.generated-hash.max-pool-size}") int maxPoolSize,
        @Value("${app.thread-pool.async.generated-hash.queue-capacity}") int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(queueCapacity);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);

        executor.setThreadNamePrefix("After-Commit-Async-");
        executor.initialize();
        return executor;
    }
}