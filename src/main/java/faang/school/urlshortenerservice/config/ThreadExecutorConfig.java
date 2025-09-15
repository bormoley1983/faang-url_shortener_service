package faang.school.urlshortenerservice.config;

import faang.school.urlshortenerservice.config.property.ThreadPoolProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
public class ThreadExecutorConfig {
    private final ThreadPoolProps props;

    @Bean(destroyMethod = "shutdown")
    public Executor hashTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.hash().corePoolSize());
        executor.setMaxPoolSize(props.hash().maxPoolSize());
        executor.setQueueCapacity(props.hash().queueCapacity());
        executor.setThreadNamePrefix(props.hash().threadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler schedulerTaskExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(props.scheduler().poolSize());
        executor.setThreadNamePrefix(props.hash().threadNamePrefix());
        executor.setRemoveOnCancelPolicy(true);
        executor.initialize();
        return executor;
    }
}
