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
@ConfigurationProperties(prefix = "thread-pool")
@Validated
@Getter
@Setter
public class ThreadPoolProperties {

    //Базовое количество потоков в пуле

    @NotNull(message = "thread-pool.core-pool-size must be configured")
    @Min(value = 1, message = "thread-pool.core-pool-size must be at least 1")
    @Max(value = 10, message = "thread-pool.core-pool-size cannot exceed 10")
    private Integer corePoolSize;

    //Максимальное количество потоков в пуле

    @NotNull(message = "thread-pool.max-pool-size must be configured")
    @Min(value = 1, message = "thread-pool.max-pool-size must be at least 1")
    @Max(value = 100, message = "thread-pool.max-pool-size cannot exceed 100")
    private Integer maxPoolSize;

    //Размер очереди задач

    @NotNull(message = "thread-pool.queue-capacity must be configured")
    @Min(value = 1, message = "thread-pool.queue-capacity must be at least 1")
    @Max(value = 1000, message = "thread-pool.queue-capacity cannot exceed 1000")
    private Integer queueCapacity;
}
