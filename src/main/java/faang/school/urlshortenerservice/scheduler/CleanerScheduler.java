package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Шедулер для освобождения хэшей, созданных более года назад
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    private final UrlRepository urlRepository;

    @Transactional
    @Scheduled(cron = "${hashes.delete-scheduled.cron}")
    public void deleteOldHashes() {
        urlRepository.deleteOldHashes();
    }
}
