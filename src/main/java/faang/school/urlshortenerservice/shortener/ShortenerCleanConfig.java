package faang.school.urlshortenerservice.shortener;

import faang.school.urlshortenerservice.executor.ExecutorConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "scheduler.expired-urls-clean")
public class ShortenerCleanConfig {
    private String cron;
    private int batchSize;
    private int fetchLimit;
    private int BatchDelayMs;
    private ExecutorConfig executorConfig = new ExecutorConfig();

    @Getter
    @Setter
    public static class ExecutorConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private String threadNamePrefix;
    }
}
