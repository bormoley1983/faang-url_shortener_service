package faang.school.urlshortenerservice.config.cleaner;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.cleaner")
public class CleanerProperties {
    private String cron;
    private Duration retention = Duration.ofDays(365);
}