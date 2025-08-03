package faang.school.urlshortenerservice.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Repository
public class HashRepository {
    public List<String> fetchHashes(int limit) {
        return IntStream.range(0, limit)
                .mapToObj(i -> UUID.randomUUID().toString().substring(0, 6))
                .toList();
    }
}
