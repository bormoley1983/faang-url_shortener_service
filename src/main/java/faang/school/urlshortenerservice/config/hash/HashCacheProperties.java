package faang.school.urlshortenerservice.config.hash;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hash-cache")
public class HashCacheProperties {
    private int capacity;
    private int refillThresholdPercent;
    private int refillBatchSize;
}
