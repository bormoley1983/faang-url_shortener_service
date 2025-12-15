package faang.school.urlshortenerservice.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Getter
@Setter
public class AsyncConfig {

    @Value("${url-shortener.hash-generator.pool-size:5}")
    private int poolSize;

    @Value("${url-shortener.hash-cache.executor.queue-size:500}")
    private int queueSize;

    @Bean(name = "hashGeneratorExecutor")
    public Executor hashGeneratorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueSize);
        executor.setThreadNamePrefix("HashGenerator-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "hashCacheExecutor")
    public ExecutorService hashCacheExecutor() {

        return new ThreadPoolExecutor(
                poolSize,
                poolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
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
        );
    }
}
