package faang.school.urlshortenerservice.config;

import faang.school.urlshortenerservice.cache.HashCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ExecutorServiceConfig {
    @Value("${hash.generator.executor.core-pool-size}")
    private int generatorCorePoolSize;
    @Value("${hash.generator.executor.max-pool-size}")
    private int generatorMaxPoolSize;
    @Value("${hash.generator.executor.queue-capacity}")
    private int generatorQueueCapacity;

    @Value("${hash.cache.executor.core-pool-size}")
    private int cacheCorePoolSize;
    @Value("${hash.cache.executor.max-pool-size}")
    private int cacheMaxPoolSize;
    @Value("${hash.cache.executor.queue-capacity}")
    private int CacheQueueCapacity;


    @Bean("hashGeneratorExecutorService")
    public Executor hashGeneratorExecutorService() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(generatorCorePoolSize);
        executor.setMaxPoolSize(generatorMaxPoolSize);
        executor.setQueueCapacity(generatorQueueCapacity);
        executor.initialize();
        return executor;
    }

    @Bean("hashCacheExecutor")
    public Executor hashCacheExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cacheCorePoolSize);
        executor.setMaxPoolSize(cacheMaxPoolSize);
        executor.setQueueCapacity(CacheQueueCapacity);
        executor.initialize();
        return executor;
    }
}