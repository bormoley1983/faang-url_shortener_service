package faang.school.urlshortenerservice.schedule;

import faang.school.urlshortenerservice.repository.HashDao;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanerScheduler {
    private final UrlRepository urlRepository;
    private final HashDao hashDao;

    @Scheduled(cron = "${hash.cleaner.cron}")
    @Transactional
    public void cleanUpOldUrls() {
        List<String> freedHashes = urlRepository.deleteExpiredUrlsAndReturnHashes();
        log.info("Deleted {} expired URLs. Freeing {} hashes.", freedHashes.size(), freedHashes.size());
        hashDao.save(freedHashes);
    }
}
