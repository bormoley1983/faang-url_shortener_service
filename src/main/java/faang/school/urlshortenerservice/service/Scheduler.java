package faang.school.urlshortenerservice.service;

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

        try {
            runCleanJob();
            log.info("Scheduled job finished in {} millis", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Scheduled job failed", e);
            throw e;
        }
    }

    private int runCleanJob() {
        int batchSize = shortenerCleanConfig.getBatchSize();
        int fetchLimit = shortenerCleanConfig.getFetchLimit();

        int maxBatches = (int) Math.ceil((double) fetchLimit / batchSize);
        int totalDeleted = 0;
        log.info("Starting cleanup. Batch size: {}, Max batches: {}", batchSize, maxBatches);

        for (int batchNumber = 1; batchNumber <= maxBatches; batchNumber++) {
            if (Thread.currentThread().isInterrupted()) {
                log.info("Clean job interrupted");
                break;
            }

            log.debug("Processing batch {}/{}", batchNumber, maxBatches);

            int deletedInBatch = shortenerCleaner.cleanExpiredUrlsBatchSync(batchSize);
            totalDeleted += deletedInBatch;
            log.debug("Batch {}: deleted {} URLs", batchNumber, deletedInBatch);

            if (deletedInBatch < batchSize) {
                log.info("No more expired URLs found. Stopping early.");
                break;
            }

            if (batchNumber < maxBatches && shortenerCleanConfig.getBatchDelayMs() > 0) {
                try {
                    Thread.sleep(shortenerCleanConfig.getBatchDelayMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Clean job interrupted during pause");
                    break;
                }
            }
        }
        log.info("Clean job completed. Total URLs deleted: {}", totalDeleted);
        return totalDeleted;
    }
}
