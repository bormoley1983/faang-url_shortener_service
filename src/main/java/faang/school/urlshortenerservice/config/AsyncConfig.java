package faang.school.urlshortenerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "hashGeneratorExecutor")
    public Executor hashGeneratorExecutor(
            @Value("${hash.executor.core-pool-size}") int corePoolSize,
            @Value("${hash.executor.max-pool-size}") int maxPoolSize,
            @Value("${hash.executor.queue-capacity}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("hash-generator-");
        executor.initialize();
        return executor;
    }
}