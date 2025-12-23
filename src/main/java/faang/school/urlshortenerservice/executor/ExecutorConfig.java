package faang.school.urlshortenerservice.executor;

import faang.school.urlshortenerservice.shortener.ShortenerCleanConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class ExecutorConfig {
    private final HashGeneratorExecutorConfig hashGeneratorExecutorConfig;
    private final ShortenerCleanConfig shortenerCleanConfig;

    @Primary
    @Bean(name = "hashGeneratorExecutor")
    public ThreadPoolTaskExecutor hashGeneratorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(hashGeneratorExecutorConfig.getCorePoolSize());
        executor.setMaxPoolSize(hashGeneratorExecutorConfig.getMaxPoolSize());
        executor.setThreadNamePrefix(hashGeneratorExecutorConfig.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(name = "shortenerCleanerExecutor")
    public ThreadPoolTaskExecutor shortenerCleanerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(shortenerCleanConfig.getExecutorConfig().getCorePoolSize());
        executor.setMaxPoolSize(shortenerCleanConfig.getExecutorConfig().getMaxPoolSize());
        executor.setThreadNamePrefix(shortenerCleanConfig.getExecutorConfig().getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
