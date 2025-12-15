package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.service.url.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class SchedulerCleaner {
    private final UrlService urlService;

    @Scheduled(cron = "${scheduler.hash-cleaner-cron}")
    public void cleanHash() {
        log.info("Starting cron to clean unused hash, current time {}", LocalDateTime.now());
        urlService.cleanHash();
        log.info("Successful clean hash");
    }
}