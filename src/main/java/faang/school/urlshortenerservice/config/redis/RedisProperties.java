package faang.school.urlshortenerservice.config.redis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {
    @NotBlank(message = "Redis host cannot be blank or null")
    private String host;
    @Positive(message = "Redis port must be positive")
    private int port;
    private String password;
}