package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Шедулер для освобождения хэшей, созданных более года назад и дальнейшего помещения их в базу для
 * переиспользования
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanerScheduler {

    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;

    @Transactional
    @Scheduled(cron = "${hashes.delete-scheduled.cron}")
    public void deleteOldHashes() {
        List<String> deletedHashes = urlRepository.deleteAndReturnOldHashes();
        List<Hash> hashes = deletedHashes.stream()
                .map(Hash::new)
                .toList();
        hashRepository.saveAll(hashes);
    }
}
