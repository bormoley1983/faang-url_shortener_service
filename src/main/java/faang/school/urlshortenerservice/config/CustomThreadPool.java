package faang.school.urlshortenerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class CustomThreadPool {

    @Value("${threadpool.core-size:5}")
    private int corePoolSize;

    @Value("${threadpool.max-size:10}")
    private int maxPoolSize;

    @Value("${threadpool.queue-capacity:100}")
    private int queueCapacity;

    @Bean("myThreadPool")
    public ExecutorService threadPoolExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(queueCapacity));
    }
}
