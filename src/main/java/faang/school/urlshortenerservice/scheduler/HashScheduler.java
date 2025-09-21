package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.service.HashGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashScheduler {
    private final HashGenerator hashGenerator;

    @Scheduled(cron = "${schedule.cron.hash.generate}")
    public void generateHashes() {
        hashGenerator.generateBatchIfNeeded();
    }
}
