package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.config.HashConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashService {
    private final HashConfig hashConfig;
    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final HashStorageAsyncFiller hashStorageAsyncFiller;

    @Transactional
    public List<String> getFreeHashes(long count) {
        long currentRepositoryCount = hashRepository.count();

        if (currentRepositoryCount < count) {
            log.warn("Not enough hashes in repository ({} < {}), generating missing ones synchronously",
                    currentRepositoryCount, count);
            int missingCount = (int) (count - currentRepositoryCount);
            List<String> missingHashes = hashGenerator.generateHashes(missingCount);
        }

        List<String> freeHashes = hashRepository.getFreeHashesBatchWithLockAndDelete(count);
        log.debug("Left free hashes in repository after getting {}: {}", count, hashRepository.count());

        boolean needRefill = hashRepository.count() < hashConfig.getStorageUpdateCount();
        if (needRefill) {
            log.warn("Start refilling hash repository");
            hashStorageAsyncFiller.refillStorageAsync(hashConfig.getStorage().getSize());
        }

        return freeHashes;
    }
}
