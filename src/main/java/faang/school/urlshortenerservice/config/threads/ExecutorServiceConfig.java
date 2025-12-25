package faang.school.urlshortenerservice.config.threads;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorServiceConfig {

    @Value("${executor.pool-size}")
    private int poolSize;

    @Value("${executor.max-pool-size}")
    private int maxPoolSize;

    @Value("${executor.queue-capacity}")
    private int queueCapacity;

    @Value("${executor.aliveTime}")
    private long aliveTime;

    @Bean
    public ExecutorService taskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolSize,
                maxPoolSize,
                aliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
