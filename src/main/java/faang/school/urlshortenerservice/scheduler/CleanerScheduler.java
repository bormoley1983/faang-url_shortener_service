package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Класс для освобождения неиспользуемых хэшей
 *
 * @author Linempy
 * @since 16.09.2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    @Value("${app.hash.scheduler-year:1}")
    private int periodCleanUp;

    @Transactional
    @Scheduled(cron = "${app.scheduler.cleanUpUrl}")
    public void cleanUpUrl() {
        List<String> freeHashes = urlRepository.deleteOldHashesAndReturn(periodCleanUp);
        log.info("Освобождение хэшей была выполнено. Освобождено {} хэшей", freeHashes.size());
        saveIfNotEmpty(freeHashes);
    }

    private void saveIfNotEmpty(List<String> freeHashes) {
        if (!freeHashes.isEmpty()) {
            hashRepository.saveAll(freeHashes);
        }
    }
}