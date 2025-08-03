package faang.school.urlshortenerservice.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Repository
@Slf4j
public class HashRepository {
    public List<String> fetchHashes(int limit) {
        return IntStream.range(0, limit)
                .mapToObj(i -> UUID.randomUUID().toString().substring(0, 6))
                .toList();
    }

    public List<Long> getUniqueNumbers(int batchSize) {
        long base = System.currentTimeMillis();
        return java.util.stream.LongStream.range(base, base + batchSize)
                .boxed()
                .toList();
    }

    public void saveHashes(List<String> hashes) {
        hashes.forEach(hash -> log.info("Saved hash to DB: {}", hash));
    }
}