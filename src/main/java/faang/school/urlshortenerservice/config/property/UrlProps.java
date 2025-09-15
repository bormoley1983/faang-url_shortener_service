package faang.school.urlshortenerservice.config.property;

import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "url")
public record UrlProps(
        @NonNull String baseShortUrl,
        @NonNull Expiration expiration
) {
    public record Expiration(
            @DefaultValue("1") int time,
            @DefaultValue("YEARS") ChronoUnit unit,
            @DefaultValue("12") long minRequestCount
    ) {}
}
