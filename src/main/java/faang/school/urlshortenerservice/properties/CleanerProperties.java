package faang.school.urlshortenerservice.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "cleaner")
@Validated
@Getter
@Setter
public class CleanerProperties {
    @NotNull(message = "cleaner.older-than-years must be configured")
    @Min(value = 1)
    private Integer olderThanYears;

    @NotNull(message = "cleaner.cron must be configured")
    private String cron;
}
