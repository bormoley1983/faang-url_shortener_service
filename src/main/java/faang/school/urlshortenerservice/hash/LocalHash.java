package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class LocalHash {

    @Value("${hash.local.size.minimum:0.2}")
    private Double minimumSizeLocalHash;
    @Value("${hash.local.count.hash:1000}")
    private Integer countLocalHash;
    @Value("${hash.generator.hashes-in-bd.minimum}")
    private Integer minimumHashesInDd;

    private final ConcurrentLinkedQueue<Hash> localHash = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isCheckingSizeQueue = new AtomicBoolean(false);


    private final HashGenerator hashGenerator;
    private final HashRepository hashRepository;

    @PostConstruct
    public void initGenerateLocalHash() {
        hashGenerator.hashGenerator();
        checkRunning();
    }

    public Hash getLocalHash() {
        Hash hash = localHash.poll();

        if (isCheckingSizeQueue.compareAndSet(false, true)) {
            int currentSizeLocalHash = localHash.size();
            double currentOccupancyPercentage = currentSizeLocalHash / (countLocalHash * 1.0);
            if (currentOccupancyPercentage <= minimumSizeLocalHash) {
                checkRunning();
            }
            isCheckingSizeQueue.set(false);
        }
        return hash;
    }

    @Transactional
    public void generateLocalHash() {
        try {
                List<Hash> hash = hashGenerator.getHash();
                localHash.addAll(hash);
        } catch (Exception e) {
            log.error("generate local hash failed", e);
        } finally {
            isRunning.set(false);
            log.info("generate local hash finished {}", isRunning);
        }
    }

    private void checkRunning() {
        if (isRunning.compareAndSet(false, true)) {
            CompletableFuture.runAsync(this::generateLocalHash);
        }
    }

    @Transactional
    public void checkCountHashInBd() {

        Long countInBd = hashRepository.countTotal();
        if (countInBd <= minimumHashesInDd) {
            log.info("hashes in bd have {},it's not enough! launch hash generator!", countInBd);
            List<Hash> hash = hashGenerator.getHash();
            localHash.addAll(hash);
            hashGenerator.hashGenerator();
        }
        log.info("hashes in bd have {},that's enough! ------ localHash size - {}", countInBd, localHash.size());

    }
}
