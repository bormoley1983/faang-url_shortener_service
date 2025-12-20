package faang.school.urlshortenerservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "app.url-cache")
public class UrlCacheProperties {
    private Duration ttl = Duration.ofDays(30);
    private String version = "v1";
    private String module = "urlshortener";
    private String urlEntity = "url";
}