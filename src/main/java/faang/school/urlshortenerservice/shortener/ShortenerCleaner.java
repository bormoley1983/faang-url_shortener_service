package faang.school.urlshortenerservice.shortener;

import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortenerCleaner {
    private final UrlService urlService;

    @Async("shortenerCleanerExecutor")
    public void cleanExpiredUrlsBatchAsync(int limit) {
        long start = System.currentTimeMillis();
        try {
            urlService.deleteExpiredShortUrls(limit);
            log.info("Cleaned in {} millis",(System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("Failed to clean batch", e);
        }
    }
}
