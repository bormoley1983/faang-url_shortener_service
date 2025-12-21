package faang.school.urlshortenerservice.config;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@Getter
@Setter
public class AsyncConfig {

    @Value("${url-shortener.hash-generator.pool-core-size:5}")
    private int hashGeneratorCorePool;

    @Value("${url-shortener.hash-generator.pool-max-size:10}")
    private int hashGeneratorMaxPool;

    @Value("${url-shortener.hash-generator.queue-size:500}")
    private int hashGeneratorQueueSize;

    @Value("${url-shortener.hash-cache.pool-core-size:5}")
    private int hashCacheCorePool;

    @Value("${url-shortener.hash-cache.pool-max-size:10}")
    private int hashCacheMaxPool;

    @Value("${url-shortener.hash-cache.queue-size:500}")
    private int hashCacheQueueSize;

    @Bean(name = "hashGeneratorExecutor")
    public ThreadPoolTaskExecutor hashGeneratorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(hashGeneratorCorePool);
        executor.setMaxPoolSize(hashGeneratorMaxPool);
        executor.setQueueCapacity(hashGeneratorQueueSize);
        executor.setThreadNamePrefix("HashGenerator-");
        executor.setDaemon(true);

        executor.setRejectedExecutionHandler((r, exec) -> {
            ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
            log.warn("HashGenerator task rejected! Active threads={}, Queue size={}",
                    pool.getActiveCount(),
                    pool.getQueue().size());
        });

        executor.initialize();
        return executor;
    }

    @Bean(name = "hashCacheExecutor")
    public ExecutorService hashCacheExecutor() {
        AtomicInteger counter = new AtomicInteger(0);

        return new ThreadPoolExecutor(
                hashCacheCorePool,
                hashCacheMaxPool,
                30L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(hashCacheQueueSize),
                r -> {
                    Thread t = new Thread(r, "hash-cache-exec-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                },
                (r, executor) ->
                        log.warn("HashCache refill rejected (skip). Active={}, Queue={}",
                        executor.getActiveCount(),
                        executor.getQueue().size())
        );
    }
}