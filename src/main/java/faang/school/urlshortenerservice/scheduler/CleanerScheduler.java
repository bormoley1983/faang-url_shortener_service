package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.service.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanerScheduler {

    private final UrlService urlService;

    @Scheduled(cron = "${cleaner.cron}")
    public void cleanOldUrls() {
        log.info("Starting scheduled cleanup of old URLs");

        try {
            urlService.cleanOldUrls();
            log.info("Scheduled cleanup completed successfully");
        } catch (Exception e) {
            log.error("Failed to clean old URLs", e);
        }
    }
}
