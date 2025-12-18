package faang.school.urlshortenerservice;

import faang.school.urlshortenerservice.shortener.ShortenerCleanConfig;
import faang.school.urlshortenerservice.shortener.ShortenerCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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

        try {
            runCleanJob();
            log.info("Scheduled job finished in {} millis", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Scheduled job failed", e);
        }
    }

    private void runCleanJob() {
        int totalDeleted = 0;
        int batchCount = shortenerCleanConfig.getFetchLimit() / shortenerCleanConfig.getBatchSize();
        log.info("Starting cleanup. Total batches to process: {}", batchCount);

        for (int batchNumber = 1; batchNumber <= batchCount; batchNumber++) {
            if (Thread.currentThread().isInterrupted()) {
                log.info("Clean job interrupted");
                break;
            }

            log.debug("Processing batch {}/{}", batchNumber, batchCount);

            int deletedInBatch = shortenerCleaner.cleanExpiredUrlsBatchSync(
                    shortenerCleanConfig.getBatchSize()
            );

            totalDeleted += deletedInBatch;
            log.debug("Batch {}: deleted {} URLs", batchNumber, deletedInBatch);

            if (deletedInBatch < shortenerCleanConfig.getBatchSize()) {
                log.info("No more expired URLs found. Stopping early.");
                break;
            }

            if (batchNumber < batchCount) {
                try {
                    TimeUnit.MILLISECONDS.sleep(shortenerCleanConfig.getBatchDelayMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Clean job interrupted during pause");
                    break;
                }
            }
        }
        log.info("Clean job completed. Total URLs deleted: {}", totalDeleted);
    }
}
