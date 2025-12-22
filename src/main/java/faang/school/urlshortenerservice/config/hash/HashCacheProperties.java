package faang.school.urlshortenerservice.config.hash;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.hash-cache")
public class HashCacheProperties {
    private int capacity;
    private int refillThresholdPercent;
    private int refillBatchSize;
    private Duration waitTimeout = java.time.Duration.ofMillis(200);
}
