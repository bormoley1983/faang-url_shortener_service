package faang.school.urlshortenerservice.hash;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hash.storage")
public class HashStorageProperties {

    @Min(1)
    private int maxSizeCachedQueue;

    @Min(0)
    @Max(100)
    private int thresholdToRefillCachedQueue;

    @Min(1)
    private int hashRange;

    @Min(0)
    private int initialWarmupAmount;

    @Min(1)
    private int rangeUniqueNumbers;

    @Min(1)
    private int batchSizeGeneratedHashes;

    @Min(1)
    private int batchSizeRegeneratedHashes;
}