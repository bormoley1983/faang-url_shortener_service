package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocalCache {

    @Value("${hash.storage.INITIAL_WARMUP_AMOUNT:1000}")
    private final int INITIAL_WARMUP_AMOUNT;

    private final HashRepository hashRepository;
    private final HashGenerator hashGenerator;
    private final HashCache hashCache;

    @Value("${hash.storage.max-size}")
    private int warmupSize;

    @PostConstruct
    public void warmUp() {
        getHashes(INITIAL_WARMUP_AMOUNT);
    }

    @Async
    @Transactional
    public List<Hash> getHashes(long amount) {
        List<Hash> hashes = hashRepository.getAndDelete(amount);
        if (hashes.size() < amount) {
            hashGenerator.generateHash();
            hashes.addAll(getHashes(amount - hashes.size()));
        }
        return hashes;
    }
}
