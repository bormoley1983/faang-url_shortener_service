package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.config.HashConfig;
import faang.school.urlshortenerservice.util.Base62Encoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashGenerator {
    private static final int HASH_GENERATION_LOCK_ID = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SHUFFLE_THRESHOLD = 10_000;

    private final Base62Encoder base62Encoder;
    private final HashRepository hashRepository;
    private final HashConfig hashConfig;

    public List<String> generateHashes(int count) {
        return base62Encoder.encodeBatch(getNextSequenceBatch(count));
    }

    @Transactional
    public void refillHashStorage(int count) {
        boolean lockedCurrentThread = hashRepository.tryLock(HASH_GENERATION_LOCK_ID);

        if (!lockedCurrentThread) {
            log.info("Another instance refilling storage.");
            return;
        }

        try {
            log.info("Starting refill hash storage");
            long start = System.currentTimeMillis();

            List<Hash> newHashes = generateHashes(count).stream()
                    .map(Hash::new)
                    .toList();

            hashRepository.saveAll(newHashes);
            log.info("Generated and saved {} in {} millis", newHashes.size(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Error during storage refill", e);
        } finally {
            hashRepository.unLock(HASH_GENERATION_LOCK_ID);
        }
    }

    @Transactional
    public List<String> getFreeHashes(long count) {
        List<String> freeHashes = hashRepository.getFreeHashesBatchWithLockAndDelete(count);

        long freeHashesCount = hashRepository.countFreeHashes();
        log.debug("Left free hashes in storage: {}", freeHashesCount);

        if (freeHashes.size() < count) {
            int missingCount = (int) count - freeHashes.size();
            log.warn("Not enough free hashes for cache, generate missing {}", missingCount);
            List<String> missingHashes = generateHashes(missingCount);
            freeHashes.addAll(missingHashes);
            freeHashesCount = hashRepository.countFreeHashes();
        }

        boolean needRefill = freeHashesCount < hashConfig.getStorageUpdateCount();
        if (needRefill) {
            log.warn("Start refilling hash repository (free: {}, threshold: {})",
                    freeHashesCount, hashConfig.getStorageUpdateCount());
            refillStorageAsync(hashConfig.getStorage().getSize());
        }
        return freeHashes;
    }

    @Async("hashGeneratorExecutor")
    public void refillStorageAsync(int count) {
        refillHashStorage(count);
    }

    private List<Long> getNextSequenceBatch(int count) {
        List<Long> sequence = hashRepository.getNextSequenceBatchValues(count);

        if (count >= SHUFFLE_THRESHOLD) {
            Collections.shuffle(sequence);
        } else {
            secureShuffle(sequence);
        }
        return sequence;
    }

    private void secureShuffle(List<Long> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            Collections.swap(list, i, j);
        }
    }
}
