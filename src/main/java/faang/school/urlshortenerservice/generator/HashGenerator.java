package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.UrlHashJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class HashGenerator {
    private static final long A = 7_777_777_777L;
    private static final long B = 123_456_789L;
    private static final long M = 56_800_235_584L;
    private static String BASE_62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final UrlHashJdbcRepository urlHashJdbcRepository;

    @Value("${hash.rangeSequence:10000}")
    private int rangeSequence;

    @Transactional
    public void generateHash() {
        List<Long> range = urlHashJdbcRepository.getNextRangeSequence(rangeSequence);

        List<String> hashes = range.stream()
                .map(this::getShuffledBase62)
                .toList();

        urlHashJdbcRepository.batchInsertHashes(hashes);
    }

    public List<String> getHashes(long count) {
        List<Hash> hashes = urlHashJdbcRepository.findAndDelete(count);

        if (hashes.size() < count) {
            generateHash();
            hashes.addAll(urlHashJdbcRepository.findAndDelete(count - hashes.size()));
        }

        return hashes.stream().map(Hash::getHash).toList();
    }

    @Async("hashGeneratorExecutor")
    public CompletableFuture<List<String>> getHashesAsync(long count) {
        return CompletableFuture.completedFuture(getHashes(count));
    }

    private String getShuffledBase62(long number) {
        long mixed = (number * A + B) % M;
        String base62 = base62Encoding(mixed);
        return String.format("%6s", base62).replace(' ', '0');
    }

    private String base62Encoding(long number) {
        if (number == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % 62);
            sb.append(BASE_62_CHARACTERS.charAt(remainder));
            number /= 62;
        }
        return sb.reverse().toString();
    }
}