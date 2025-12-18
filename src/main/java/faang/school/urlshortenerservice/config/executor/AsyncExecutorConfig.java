package faang.school.urlshortenerservice.config.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncExecutorConfig {

    @Value("${analytic.executor.corePoolSize:5}")
    private int analyticCorePoolSize;

    @Value("${analytic.executor.maxPoolSize:10}")
    private int analyticMaxPoolSize;

    @Value("${analytic.executor.queueCapacity:500}")
    private int analyticQueueCapacity;

    @Value("${analytic.executor.threadNamePrefix:AnalyticExecutor-}")
    private String analyticThreadNamePrefix;

    @Bean(name = "analyticExecutor")
    public Executor analyticExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(analyticCorePoolSize);
        executor.setMaxPoolSize(analyticMaxPoolSize);
        executor.setQueueCapacity(analyticQueueCapacity);
        executor.setThreadNamePrefix(analyticThreadNamePrefix);
        executor.initialize();
        return executor;
    }




    @Value("${hash.executor.corePoolSize:10}")
    private int hashCorePoolSize;

    @Value("${hash.executor.maxPoolSize:20}")
    private int hashMaxPoolSize;

    @Value("${hash.executor.queueCapacity:1000}")
    private int hashQueueCapacity;

    @Value("${hash.executor.threadNamePrefix:HashExecutor-}")
    private String hashThreadNamePrefix;

    @Bean(name = "hashGeneratorExecutor")
    public Executor hashGeneratorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(hashCorePoolSize);
        executor.setMaxPoolSize(hashMaxPoolSize);
        executor.setQueueCapacity(hashQueueCapacity);
        executor.setThreadNamePrefix(hashThreadNamePrefix);
        executor.initialize();
        return executor;
    }
}