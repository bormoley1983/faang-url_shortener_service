package faang.school.urlshortenerservice.config.executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

@Component
public class ExecutorsConfig {
    @Value("${thread.core-pool-size}")
    private int corePoolSize;
    @Value("${thread.max-pool-size}")
    private int maxPoolSize;
    @Value("${thread.queue-capacity}")
    private int queueCapacity;
    @Value("${thread.await-termination-seconds}")
    private int awaitTerminationSecond;


    @Bean(name = "threadExecutor")
    public TaskExecutor threadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("thread-hash:");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSecond);

        executor.initialize();
        return executor;
    }
}