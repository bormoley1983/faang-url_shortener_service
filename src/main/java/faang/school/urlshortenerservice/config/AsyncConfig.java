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
import java.util.concurrent.ThreadFactory;
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

        executor.setThreadFactory(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        executor.initialize();
        ThreadPoolExecutor pool = executor.getThreadPoolExecutor();

        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn("HashGenerator task rejected! Active threads={}, Queue size={}",
                    pool.getActiveCount(),
                    pool.getQueue().size());
            r.run();
        });

        return executor;
    }

    @Bean(name = "hashCacheExecutor")
    public ExecutorService hashCacheExecutor() {
        return new ThreadPoolExecutor(
                hashCacheCorePool,
                hashCacheMaxPool,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(hashCacheQueueSize),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("hash-cache-exec-" + counter.incrementAndGet());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        ) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                super.beforeExecute(t, r);
                log.debug("Executing task in {} | Active threads={}, Queue size={}",
                        t.getName(), getActiveCount(), getQueue().size());
            }
        };
    }
}
