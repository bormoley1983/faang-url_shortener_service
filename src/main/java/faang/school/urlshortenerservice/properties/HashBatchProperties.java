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
@ConfigurationProperties(prefix = "hash")
@Validated
@Getter
@Setter
public class HashBatchProperties {

    @NotNull(message = "hash.batch-size must be configured in application.yml")
    @Min(value = 1, message = "hash.batch-size must be at least 1")
    @Max(value = 10000, message = "hash.batch-size cannot exceed 10000")
    private Integer batchSize;

    @NotNull(message = "hash.hash-generation-size must be configured")
    @Min(value = 100, message = "hash.hash-generation-size must be at least 100")
    @Max(value = 10000, message = "hash.hash-generation-size cannot exceed 10000")
    private Integer hashGenerationSize;
}
