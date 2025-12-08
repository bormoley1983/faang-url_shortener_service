package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    @Value("${hash.generator.max-range}")
    private final Integer maxRange;

    private final HashRepository hashRepository;

    public void hashGenerator() {
        hashRepository.getNextRange(maxRange);
        hashRepository.save(new Hash("hashMock"));
    }

    public List<String> getHash() {
        return List.of("hashMock");
    }
}
