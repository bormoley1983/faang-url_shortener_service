package faang.school.urlshortenerservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "url-shortener")
public class UrlShortenerProperties {
    private String domain;
    private Cleaner cleaner = new Cleaner();
    private HashCache hashCache = new HashCache();
    private HashGenerator hashGenerator = new HashGenerator();
    private Cache cache = new Cache();

    @Data
    public static class Cleaner {
        private String cron;
    }

    @Data
    public static class HashCache {
        private int maxSize;
        private double refillThresholdPercent;
        private Executor executor = new Executor();

        @Data
        public static class Executor {
            private int corePoolSize;
            private int maxPoolSize;
            private int queueCapacity;
        }
    }

    @Data
    public static class HashGenerator {
        private int batchSize;
        private Executor executor = new Executor();

        @Data
        public static class Executor {
            private int corePoolSize;
            private int maxPoolSize;
            private int queueCapacity;
        }
    }

    @Data
    public static class Cache {
        private int ttlHours;
    }
}

