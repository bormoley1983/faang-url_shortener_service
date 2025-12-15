package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.service.url.UrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SchedulerCheckHash {
    private final UrlService urlService;
    private final HashGenerator hashGenerator;
    @Value("${generator.maxRange}")
    private long hashCapacity;
    @Value("${hash.min-capacity-percent}")
    private int minCapacityPercent;

    @Scheduled(cron = "${scheduler.check-hash-cron}")
    public void checkCountHash() {
        log.info("Starting scheduler to check database capacity hash");
        long count = urlService.countHashRepository();
        double percentCapacityDb = count / (hashCapacity / 100.0);
        if (percentCapacityDb < minCapacityPercent) {
            log.info("The number of free hashes in the {} database < 20, start hash generation", percentCapacityDb);
            startGenerate();
        }
        log.info("Verification successful");
    }

    private void startGenerate() {
        hashGenerator.generateHash();
    }
}