package faang.school.urlshortenerservice.storage;

import faang.school.urlshortenerservice.service.HashService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
@RequiredArgsConstructor
public class HashMemoryCache {
    private final HashService hashService;
    private int defaultCacheSize = 1000;
    private double defaultCacheLoadFactor = 0.75;
    private final AtomicBoolean isFull = new AtomicBoolean(false);
    private Queue<String> hashCacheQueue;

    @PostConstruct
    public void init() {
        this.hashCacheQueue = new LinkedBlockingQueue<>(defaultCacheSize);
        hashCacheQueue.addAll();
    }

}
