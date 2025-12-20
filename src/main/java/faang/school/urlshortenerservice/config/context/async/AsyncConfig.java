package faang.school.urlshortenerservice.config.context.async;

import faang.school.urlshortenerservice.properties.ThreadPoolProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class AsyncConfig {

    private final ThreadPoolProperties threadPoolProperties;

    @Bean(name = "hashGeneratorThreadPool")
    public Executor hashGeneratorThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        executor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setThreadNamePrefix("hash-generator-");
        executor.initialize();

        log.info("Initialized hash generator thread pool: core={}, max={}, queue={}",
                threadPoolProperties.getCorePoolSize(),
                threadPoolProperties.getMaxPoolSize(),
                threadPoolProperties.getQueueCapacity());

        return executor;
    }
}
