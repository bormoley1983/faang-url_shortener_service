package faang.school.urlshortenerservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    @Bean(name = "hashCacheExecutor")
    public ExecutorService hashCacheExecutor(
            @Value("${hash.cache.executor.core-pool-size}") int corePoolSize
    ) {
        return Executors.newFixedThreadPool(corePoolSize, r -> {
            Thread thread = new Thread(r);
            thread.setName("hash-cache-");
            return thread;
        });
    }
}