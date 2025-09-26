package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    @Scheduled(cron = "${url-shortener.cleaner.cron}")
    @Transactional
    public void cleanOldUrls() {
        log.info("Starting cleanup of old URLs");

        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(1);

        try {
            List<String> releasedHashes = urlRepository.deleteOldUrlsAndReturnHashes(cutoffDate);

            if (!releasedHashes.isEmpty()) {
                hashRepository.saveReleasedHashes(releasedHashes);
                log.info("Successfully cleaned {} old URLs and returned their hashes to pool",
                        releasedHashes.size());
            } else {
                log.info("No old URLs found for cleanup");
            }

        } catch (Exception e) {
            log.error("Failed to clean old URLs", e);
            throw e;
        }
    }
}