package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashCache {

    private static final AtomicBoolean isRefilling = new AtomicBoolean(false);
    private static ConcurrentLinkedQueue<String> hashes = new ConcurrentLinkedQueue<>();
    private static Long queueSize = 100L;
    private final HashGenerator hashGenerator;
    private final JdbcTemplate jdbcTemplate;
    private final HashRepository hashRepository;

    public String getHash() {
        if (hashes.size() < queueSize / 5 && isRefilling.compareAndSet(false, true)) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<String> generatedHashes = jdbcTemplate.queryForList("SELECT hash FROM hash LIMIT 5000", String.class);
                    hashes.addAll(generatedHashes);
                    hashRepository.deleteAllByIdInBatch(generatedHashes);
                    queueSize = (long) hashes.size();

                    hashGenerator.generateHashes();
                } finally {
                    isRefilling.set(false);
                }
            });
        }
        return hashes.poll();
    }
}
