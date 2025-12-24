package faang.school.url_shortener_service.hash;

import faang.school.url_shortener_service.entity.Hash;
import faang.school.url_shortener_service.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Component
public class HashGenerator {

    @Value("${hash-generator.batch-size}")
    private int batchSize;

    @Value("${hash-generator.generation-batch-size}")
    private int generationBatchSize;

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();

    private final HashRepository hashRepository;

    public List<Hash> getHash() {
        List<Hash> hash = hashRepository.deleteAndReturnFirstN(batchSize);
        List<Hash> mutableHash = new ArrayList<>(hash);
        Collections.shuffle(mutableHash, ThreadLocalRandom.current());
        return mutableHash;
    }

    public void generateHash() {
        List<Long> result = hashRepository.getNextRange(generationBatchSize);
        List<Hash> resultListHash = generateHashByBase62(result);
        hashRepository.saveAll(resultListHash);

    }

    public List<Hash> generateHashByBase62(List<Long> listNumbers) {

        return listNumbers.parallelStream()
                .map(number -> new Hash(encodeBase62(number)))
                .toList();
    }

    private String encodeBase62(Long number) {

        StringBuilder result = new StringBuilder();
        long temp = number;

        while (temp > 0) {
            int remainder = (int) (temp % BASE);
            result.insert(0, BASE62_CHARS.charAt(remainder));
            temp = temp / BASE;
        }

        return result.toString();
    }

    public void scheduler() {
        Long count = hashRepository.countTotal();
        if (count <=400) {
            generateHash();
        }
        log.info("Scheduler has run {}", count);
    }
}