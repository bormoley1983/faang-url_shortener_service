package faang.school.urlshortenerservice.util;

import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import faang.school.urlshortenerservice.model.Hash;

@RequiredArgsConstructor
@Slf4j
@Service
public class HashGenerator {
    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;

    @Value("${encoder.settings.batchSize}")
    private int batchSize;

    @Async("HashesGeneratorThreadPool")
    public void generateBatch() {
        List<Long> randomNumbersList = hashRepository.getUniqueNumbers(batchSize);

        if (randomNumbersList.isEmpty()) {
            throw new RuntimeException("There are no free Numbers for generating new hashes!");
        }

        List<Hash> hashList = base62Encoder.encodeFixed(randomNumbersList).stream()
            .map(Hash::new)
            .collect(Collectors.toList());

        hashRepository.saveAll(hashList);
    }

    public String encodeNumber(Long number) {
    // Assuming you have a Base62Encoder with a method encodeFixed that returns a 6-char string
    return base62Encoder.encodeFixed(number);
}
}
