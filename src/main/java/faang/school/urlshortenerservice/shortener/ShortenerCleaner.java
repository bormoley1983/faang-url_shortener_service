package faang.school.urlshortenerservice.shortener;

import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShortenerCleaner {
    private final UrlService urlService;

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public int cleanExpiredUrlsBatchSync(int limit) {
        long start = System.currentTimeMillis();
        int deletedCount = urlService.deleteExpiredShortUrls(limit);
        log.debug("Cleaned {} URLs in {} ms", deletedCount, System.currentTimeMillis() - start);
        return deletedCount;
    }

    @Async("shortenerCleanerExecutor")
    public CompletableFuture<Integer> cleanExpiredUrlsBatchAsync(int limit) {
        return CompletableFuture.completedFuture(cleanExpiredUrlsBatchSync(limit));
    }

    @Async("shortenerCleanerExecutor")
    public CompletableFuture<Integer> cleanExpiredUrlsBatchSafe(int limit) {
        try {
            int deletedCount = cleanExpiredUrlsBatchSync(limit);
            return CompletableFuture.completedFuture(deletedCount);
        } catch (Exception e) {
            log.error("Failed to clean batch", e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
