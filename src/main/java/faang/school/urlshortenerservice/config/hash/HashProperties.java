package faang.school.urlshortenerservice.config.hash;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "hash")
public class HashProperties {

    @Min(1)
    private int batchSize;
}
