package faang.school.urlshortenerservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Локальный кэш для быстрого получения хэшей
 *
 * @author Linempy
 * @since 13.09.2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalCache {

    private final HashGenerator hashGenerator;

    private Queue<String> hashes;
    private final AtomicBoolean isFilling = new AtomicBoolean(false);

    @Value("${app.batch.max-size-capacity:10000}")
    private int maxCapacityHash;

    @Value("${app.batch.getting-hash:5000}")
    private int gettingHash;

    @Value("${app.batch.percent:20}")
    private int fillPercent;

    @PostConstruct
    public void init() {
        hashes = new ArrayBlockingQueue<>(maxCapacityHash);
        hashes.addAll(hashGenerator.getHashes(gettingHash));
    }

    public String getHash() {
        if (isRefill()) {
            if (isFilling.compareAndSet(false, true)) {
                hashGenerator.getHashesAsync(gettingHash)
                        .thenAccept(hashes::addAll)
                        .thenRun(() -> isFilling.set(false));
            }
        }

        return hashes.poll();
    }

    private boolean isRefill() {
        return ((double) hashes.size() / maxCapacityHash) * 100 < fillPercent;
    }
}