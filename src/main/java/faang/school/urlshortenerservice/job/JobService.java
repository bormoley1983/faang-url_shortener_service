package faang.school.urlshortenerservice.job;

import faang.school.urlshortenerservice.hash.HashGenerator;
import faang.school.urlshortenerservice.service.ShortenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JobService {

    private final ShortenerService shortenerService;
    private final HashGenerator hashGenerator;

    @Scheduled(cron = "${app.sheduled.time.cleaner}")
    public void jobCleanerUrlFromBd() {
        shortenerService.cleanerUrlBd();
    }

    @Scheduled(
            cron = "${app.sheduled.check.hash}")
    private void jobCounterHashInHashBd() {
        hashGenerator.checkCountHashInBd();
    }
}
