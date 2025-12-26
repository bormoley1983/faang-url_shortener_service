package faang.school.urlshortenerservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashCache {

    @Value("${hash.cash.capacity:5000}")
    private int capacity;

    @Value("${cash.percent:20}")
    private short percent;
    private AtomicBoolean inProcess;
    private final HashGenerator hashGenerator;
    private final AcyncGetHashes asyncGetHashes;

    private Queue<String> hashes;

    @PostConstruct
    public void init() {
        inProcess = new AtomicBoolean(false);
        hashes = new ArrayBlockingQueue<>(capacity);
        hashes.addAll(hashGenerator.getHashes(capacity));
        log.info("HashCash initialized");
    }

    public String getHash() {
        if (hashes.size() < capacity * percent / 100 &&
                !inProcess.getAndSet(true)) {
            asyncGetHashes.getHashesAsync(capacity - hashes.size())
                    .thenAccept(hashes::addAll).thenRun(() -> inProcess.set(false));
        }
        return hashes.poll();
    }
}