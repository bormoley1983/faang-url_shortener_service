package faang.school.urlshortenerservice.localcache;

import faang.school.urlshortenerservice.generator.HashGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
public class LocalCache {
    @Value("${hash.cache.capacity:5000}")
    private int capacity;
    @Value("${hash.cache.fill.percent:30}")
    private int percent;
    private final HashGenerator hashGenerator;
    private Queue<String> hashes;
    private final AtomicBoolean isFilling = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        this.hashes = new ArrayBlockingQueue<>(capacity);
        hashes.addAll(hashGenerator.getHashes(capacity));
    }

    public String getHash() {
        if (needFill()) {
            if (isFilling.compareAndSet(false, true)) {
                hashGenerator.getHashesAsync(capacity)
                        .thenAccept(list -> {
                            for (String hash : list) {
                                hashes.offer(hash);
                            }
                        })
                        .thenRun(() -> isFilling.set(false));
            }
        }

        return hashes.poll();
    }

    private boolean needFill() {
        int threshold = (int) (capacity * (percent / 100.0));
        return hashes.size() < threshold;
    }
}
