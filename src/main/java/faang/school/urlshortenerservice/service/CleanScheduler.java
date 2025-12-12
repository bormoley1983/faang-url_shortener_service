package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanScheduler {

    private final HashRepository hashRepository;
    private final UrlRepository urlRepository;

    @Transactional
    @Scheduled(cron = "${scheduler.tasks.cleaner.cron:0 0 1 * * *}")
    public void clean() {
        List<Url> oldUrls = urlRepository.deleteOldUrls();
        List<Hash> oldHashes = oldUrls.stream()
                .map(Url::getHash)
                .map(Hash::new)
                .toList();
        hashRepository.saveAll(oldHashes);
    }
}