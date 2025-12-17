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

    @Bean
    public ExecutorService taskExecutor() {
        return new ThreadPoolExecutor(
                poolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity));// TODO что делать , чтобы не потерять задачи, если очередь полная
    }
}
