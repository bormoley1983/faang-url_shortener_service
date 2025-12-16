package faang.school.urlshortenerservice.util;


import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    @Value("${hash-cleaner.time-ago.year}")
    private int yearsAgo;
    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    @Transactional
    @Scheduled(cron = "${hash-cleaner.cron}")
    public void cleanOldUrls() {
        List<Url> urls = urlRepository.findAllByCreatedAtBefore(LocalDateTime.now().minusYears(yearsAgo));
        List<String> hashes = urls.stream()
                .map(Url::getHash)
                .toList();
        hashRepository.save(hashes);
        urlRepository.deleteAllInBatch(urls);
    }
}
