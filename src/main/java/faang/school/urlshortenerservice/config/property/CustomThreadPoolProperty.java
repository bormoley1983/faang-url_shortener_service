package faang.school.urlshortenerservice.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "thread-pool")
public record CustomThreadPoolProperty(
        @DefaultValue("5") int coreSize,
        @DefaultValue("10") int maxSize,
        @DefaultValue("100") int queueCapacity
) {
}
