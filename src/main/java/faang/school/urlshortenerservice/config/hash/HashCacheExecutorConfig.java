package faang.school.urlshortenerservice.config.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class HashCacheExecutorConfig {
    private final HashCacheExecutorProperties props;

    @Bean
    public ExecutorService hashCacheExecutor() {
        return new ThreadPoolExecutor(
                props.getPoolSize(),
                props.getPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(props.getQueueCapacity()),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
