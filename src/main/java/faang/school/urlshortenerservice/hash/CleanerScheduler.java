package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import faang.school.urlshortenerservice.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    private final UrlRepository urlRepository;
    private final HashStorageProperties properties;
    private final HashRepository hashRepository;

    @Value("${scheduler.clean-period}")
    private int year;

    @Transactional
    @Scheduled(cron = "${scheduler.cleanup-cron}")
    public void reUseHash() {
        List<String> shortUrls = urlRepository.deleteExpiredAndReturnHashes(OffsetDateTime.now().minusYears(year),
                properties.getBatchSizeRegeneratedHashes());
        if (!shortUrls.isEmpty()) {
            hashRepository.saveAll(shortUrls.stream().map(Hash::new).toList());
        }
    }
}
