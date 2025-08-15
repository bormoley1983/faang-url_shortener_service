package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanerScheduler {

    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    @Value("${app.cleaner.cron}")
    private String cleanerCron;

    @Scheduled(cron = "${app.cleaner.cron}")
    @Transactional
    public void cleanOldUrls() {
        log.info("Запущена очистка старых URL (старше 1 года)");

        List<String> freedHashes = urlRepository.deleteOldUrlsAndReturnHashes();

        if (freedHashes.isEmpty()) {
            log.info("Нет устаревших ссылок для удаления");
            return;
        }

        List<Hash> hashEntities = freedHashes.stream()
                .map(h -> Hash.builder().hashValue(h).build())
                .toList();

        hashRepository.saveAll(hashEntities);

        log.info("Освобождено и возвращено в пул {} хэшей", freedHashes.size());
    }
}