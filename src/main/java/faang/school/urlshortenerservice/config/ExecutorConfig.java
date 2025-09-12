package faang.school.urlshortenerservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExecutorConfig {

    private final UrlShortenerProperties properties;

    @Bean(name = "hashCacheExecutor")
    public ExecutorService hashCacheExecutor() {
        UrlShortenerProperties.HashCache.Executor cfg = properties.getHashCache().getExecutor();

        log.info("Creating HashCache ExecutorService with core-pool-size: {}, max-pool-size: {}, queue-capacity: {}",
                cfg.getCorePoolSize(), cfg.getMaxPoolSize(), cfg.getQueueCapacity());

        return new ThreadPoolExecutor(
                cfg.getCorePoolSize(),
                cfg.getMaxPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(cfg.getQueueCapacity())
        );
    }

    @Bean(name = "hashGeneratorExecutor")
    public ExecutorService hashGeneratorExecutor() {
        UrlShortenerProperties.HashGenerator.Executor cfg = properties.getHashGenerator().getExecutor();

        log.info("Creating HashGenerator ExecutorService with core-pool-size: {}, max-pool-size: {}, queue-capacity: {}",
                cfg.getCorePoolSize(), cfg.getMaxPoolSize(), cfg.getQueueCapacity());

        return new ThreadPoolExecutor(
                cfg.getCorePoolSize(),
                cfg.getMaxPoolSize(),
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(cfg.getQueueCapacity())
        );
    }
}