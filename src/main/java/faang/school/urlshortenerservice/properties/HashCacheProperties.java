package faang.school.urlshortenerservice.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "cache")
@Validated
@Getter
@Setter
public class HashCacheProperties {
    @NotNull(message = "cache-size must be configured in application.yml")
    @Min(value = 1, message = "cache-size must be at least 1")
    @Max(value = 1000, message = "cache-size cannot exceed 1000")
    private Integer cacheSize;

    @NotNull(message = "refill-threshold-percent must be configured")
    @Min(value = 1, message = "refill-threshold-percent must be at least 1")
    @Max(value = 100, message = "refill-threshold-percent cannot exceed 100")
    private Integer refillThresholdPercent;
}
