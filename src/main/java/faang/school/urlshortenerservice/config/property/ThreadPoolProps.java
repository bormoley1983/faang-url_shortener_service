package faang.school.urlshortenerservice.config.property;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "thread-pool")
public record ThreadPoolProps(
        @NonNull Scheduler scheduler,
        @NonNull Hash hash
) {
    public record Scheduler(
            @DefaultValue("2") int poolSize,
            @DefaultValue("scheduler-thread-") String threadNamePrefix
    ) {}

    public record Hash(
            @DefaultValue("2") int corePoolSize,
            @DefaultValue("7") int maxPoolSize,
            @DefaultValue("20") int queueCapacity,
            @DefaultValue("hash-thread-") String threadNamePrefix
    ) {}
}
