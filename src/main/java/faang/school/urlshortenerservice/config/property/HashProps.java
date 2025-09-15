package faang.school.urlshortenerservice.config.property;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "hash")
public record HashProps(
        @DefaultValue("10") int batchSize,
        @DefaultValue("5000") int minStored,
        @NonNull LocalCache local
) {
    public record LocalCache(
            @DefaultValue("1000") int capacity,
            @DefaultValue("20") int minFillPercent
    ) {}
}
