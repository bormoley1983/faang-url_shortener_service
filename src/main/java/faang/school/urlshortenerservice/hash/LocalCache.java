package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocalCache {

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;

    @Async
    @Transactional
    public List<Hash> getHashes(long amount) {
        List<Hash> hashes = hashRepository.getAndDelete(amount);

        if (hashes.size() < amount) {
            hashGenerator.generateHash();
            hashes.addAll(hashRepository.getAndDelete(amount - hashes.size()));
        }
        return hashes;
    }
}
