package faang.school.urlshortenerservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class HashGeneratorAsyncConfig {

    @Value("${url-shortener-service.hash-generator.thread-pool.core-size}")
    private int corePoolSize;

    @Value("${url-shortener-service.hash-generator.thread-pool.max-size}")
    private int maxPoolSize;

    @Value("${url-shortener-service.hash-generator.thread-pool.queue-capacity}")
    private int queueCapacity;

    @Value("${url-shortener-service.hash-generator.thread-pool.thread-name-prefix}")
    private String threadNamePrefix;

    @Bean(name = "hashGeneratorTaskExecutor")
    public Executor hashGeneratorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
