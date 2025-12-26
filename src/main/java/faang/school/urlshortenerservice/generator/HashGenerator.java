package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private static final String BASE_62_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\"";

    private final HashRepository repository;

    @Value("${hash.range:10000}")
    private int maxRange;

    @Transactional
    @Scheduled(cron = "${hash.cron:0 0 0 * * *}")
    public void generateHash() {
        List<Long> range = repository.getNextRange(maxRange);
        List<Hash> hashes = range.stream()
                .map(this::applyBase62Encoding)
                .map(Hash::new) // пришлось создавать отдельный конструктор под String в Hash
                .toList();
        repository.saveAll(hashes);
    }

    @Transactional
    public List<String> getHashes(long amount) { // <- почему стринг вместо hash в листе и почему не сам лист вместо future. Почему потом все равно поменяли на лист
        List<Hash> hashes = repository.findAndDelete(amount);
        if (hashes.size() < amount) {
            generateHash();
            hashes.addAll(repository.findAndDelete(amount - hashes.size()));
        }
        return (hashes.stream().map(Hash::getHash).toList());
    }

    @Async("hashGeneratorExecutor")
    public CompletableFuture<List<String>> getHashesAsync(long amount) {
        return CompletableFuture.completedFuture(getHashes(amount));
    }

    private String applyBase62Encoding(long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE_62_CHARACTERS.charAt((int) (number % BASE_62_CHARACTERS.length())));
            number /= HashGenerator.BASE_62_CHARACTERS.length();
        }
        return sb.toString();
    }
}



