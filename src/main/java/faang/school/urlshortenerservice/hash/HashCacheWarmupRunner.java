package faang.school.urlshortenerservice.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HashCacheWarmupRunner {

    private final LocalCache localCache;
    private final HashCache hashCache;
    private final HashStorageProperties properties;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        localCache.getHashes(properties.getInitialWarmupAmount())
                .forEach(hashCache::put);
    }
}
