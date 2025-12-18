package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlHashJdbcRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class CleanerScheduler {
    @Value("${url.ttl-days:30}")
    private long ttlDays;

    private final UrlRepository urlRepository;
    private final UrlHashJdbcRepository urlHashJdbcRepository;
    private final UrlCacheRepository urlCacheRepository;

    @Scheduled(cron = "${scheduler.cleaner.cron}")
    @Transactional
    public void cleanOldUrls() {
        Instant cutoffDate = Instant.now().minus(ttlDays, ChronoUnit.DAYS);

        List<Url> oldUrls = urlRepository.findAllByExpiresAtBefore(cutoffDate);

        if (!oldUrls.isEmpty()) {
            List<String> oldHashes = oldUrls.stream()
                    .map(Url::getHash)
                    .toList();

            urlHashJdbcRepository.batchInsertHashes(oldHashes);

            urlRepository.deleteAll(oldUrls);

            oldHashes.forEach(urlCacheRepository::delete);

            log.info("Cleanup: removed {} old URLs, returned {} hashes to the pool",
                    oldUrls.size(), oldHashes.size());
        } else {
            log.info("Cleanup: no old URLs found for deletion");
        }
    }
}