package faang.school.urlshortenerservice.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hash")
@Getter
@Setter
public class HashConfig {
    private Storage storage = new Storage();
    private Cache cache = new Cache();

    @Getter
    @Setter
    public static class Storage {
        private int size;
        private double updatePercentage;
    }

    @Getter
    @Setter
    public static class Cache {
        private int size;
        private double updatePercentage;
    }

    public long getStorageUpdateCount() {
        return (long) ((double) storage.size * storage.updatePercentage / 100);
    }

    public long getCacheUpdateCount() {
        return (long) ((double) cache.size * cache.updatePercentage / 100);
    }
}
