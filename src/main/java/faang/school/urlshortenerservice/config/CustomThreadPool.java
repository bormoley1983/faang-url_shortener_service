package faang.school.urlshortenerservice.config;

import faang.school.urlshortenerservice.config.property.CustomThreadPoolProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class CustomThreadPool {

    private final CustomThreadPoolProperty customThreadPoolProperty;

    @Bean("taskExecutor")
    public ExecutorService threadPoolExecutor() {
        return new ThreadPoolExecutor(
                customThreadPoolProperty.coreSize(),
                customThreadPoolProperty.maxSize(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(customThreadPoolProperty.queueCapacity()));
    }
}
