package faang.school.urlshortenerservice.config.hash;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hash-cache-executor")
public class HashCacheExecutorProperties {
    private int poolSize;
    private int queueCapacity;
}