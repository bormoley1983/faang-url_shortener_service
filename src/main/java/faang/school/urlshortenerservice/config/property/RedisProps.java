package faang.school.urlshortenerservice.config.property;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProps(
        @DefaultValue("1") int ttl,
        @DefaultValue("DAYS") ChronoUnit ttlUnit,
        @DefaultValue("60") int defaultTtl,
        @DefaultValue("MINUTES") ChronoUnit defaultTtlUnit,
        @NonNull Cache cache
) {
    public record Cache(
            @DefaultValue("url") String url
    ) {}
}
