package faang.school.urlshortenerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {
    @Value("${hash.cache.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${hash.cache.executor.max-pool-size:4}")
    private int maxPoolSize;

    @Value("${hash.cache.executor.queue-capacity:100}")
    private int queueCapacity;

    @Value("${hash.cache.executor.thread-name-prefix:hash-cache-}")
    private String threadNamePrefix;

    @Value("${hash.cache.executor.keep-alive-time:60}")
    private long keepAliveTime;

    @Bean("hashCacheExecutor")
    public ExecutorService hashCacheExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
