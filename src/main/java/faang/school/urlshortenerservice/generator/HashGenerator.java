package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.exception.GenerateHashesException;
import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    @Value(value = "${generator.maxRange}")
    private int maxRange;

    @Transactional
    public List<String> generateHash() {
        log.debug("Starting generate hash");
        List<Long> range = hashRepository.getNextRange(maxRange);
        log.debug("Obtained values from the database {}", range.size());
        List<String> hashes = range.stream()
                .map(base62Encoder::encodeToBase62)
                .toList();
        if (hashes.isEmpty()) {
            throw new GenerateHashesException("Error with generate hash is empty");
        }
        return hashes;
    }

    @Transactional
    public List<String> getHashes(long hashLimit) {
        List<Hash> hashes = new ArrayList<>(hashRepository.findAndDelete(hashLimit));
        if (hashes.size() < hashLimit) {
            generateHash();
            hashes.addAll(hashRepository.findAndDelete(hashLimit - hashes.size()));
        }
        return hashes.stream()
                .map(Hash::getHash)
                .toList();
    }
}