package faang.school.urlshortenerservice;

import faang.school.urlshortenerservice.shortener.ShortenerCleanConfig;
import faang.school.urlshortenerservice.shortener.ShortenerCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Scheduler {
    private final ShortenerCleaner shortenerCleaner;
    private final ShortenerCleanConfig shortenerCleanConfig;

    @Scheduled(cron = "${scheduler.expired-urls-clean.cron}", zone = "${scheduler.expired-urls-clean.zone}")
    public void clearExpiredUrls() {
        long start = System.currentTimeMillis();
        log.info("Scheduled job started.");

        runCleanJob();
        log.info("Scheduled job finished in {} millis", System.currentTimeMillis() - start);
    }

    private void runCleanJob() {
        int attempt = 0;
        int maxAttempts = shortenerCleanConfig.getFetchLimit() / shortenerCleanConfig.getBatchSize();

        while (attempt < maxAttempts) {
            attempt++;
                try {
                    shortenerCleaner.cleanExpiredUrlsBatchAsync(shortenerCleanConfig.getBatchSize());
                    } catch (Exception e) {
                    log.error("Clearing failed", e);
                    break;
                }

                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
        }
    }
}
