package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanScheduler {

    private final HashRepository hashRepository;
    private final UrlRepository urlRepository;

    @Value("${scheduler.tasks.cleaner.batch-size:1000}")
    private int batchCleanerSize;

    @Transactional
    @Scheduled(cron = "${scheduler.tasks.cleaner.cron:0 0 1 * * *}")
    public void clean() {
        long count = urlRepository.countOldUrls();
        while (count > 0) {
            if (count > batchCleanerSize) {
                cleanBatch(batchCleanerSize);
                count -= batchCleanerSize;
            } else {
                cleanBatch((int) count);
                count = 0;
            }
        }
    }

    private void cleanBatch(int batchSize) {
        List<Url> oldUrls = urlRepository.deleteOldUrls(batchSize);
        List<Hash> oldHashes = oldUrls.stream()
                .map(Url::getHash)
                .map(Hash::new)
                .toList();
        hashRepository.saveAll(oldHashes);
    }
}