package faang.school.urlshortenerservice.config.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(HashProperties.class)
@RequiredArgsConstructor
public class HashGeneratorAsyncConfig {

    public static final String HASH_GENERATOR_EXECUTOR = "hashGeneratorExecutor";

    private final HashProperties hashProperties;

    @Bean(name = HASH_GENERATOR_EXECUTOR)
    public TaskExecutor hashGeneratorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int size = hashProperties.getThreadPool().getSize();

        executor.setCorePoolSize(size);
        executor.setMaxPoolSize(size);
        executor.setQueueCapacity(hashProperties.getThreadPool().getQueueCapacity());
        executor.setThreadNamePrefix("hash-gen-");
        executor.initialize();
        return executor;
    }
}