package faang.school.urlshortenerservice.config.context.executor;

import faang.school.urlshortenerservice.properties.HashCacheExecutorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class HashCacheExecutorConfig {

    private final HashCacheExecutorProperties executorProperties;

    @Bean(name = "hashCacheExecutor")
    public Executor hashCacheExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        executor.setQueueCapacity(executorProperties.getQueueCapacity());
        executor.setThreadNamePrefix("hash-cache-");
        executor.initialize();

        log.info("Initialized hash cache executor: core={}, max={}, queue={}",
                executorProperties.getCorePoolSize(),
                executorProperties.getMaxPoolSize(),
                executorProperties.getQueueCapacity());
        return executor;
    }
}
