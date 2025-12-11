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

    @Transactional
    @Scheduled(cron = "${cleaner.scheduler.cron}")
    public void cleanUrls() {
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        log.info("Starting cleanup job, older than 1 year: {}", oneYearAgo);

        List<String> hashes = urlRepository.deleteOldUrlsAndReturnHashes(oneYearAgo);

        if (hashes.isEmpty()) {
            log.info("Cleanup job finished: no old URLs found");
            return;
        }

        hashRepository.save(hashes);

        log.info("Cleanup job finished: {} hashes returned to pool", hashes.size());
    }
}