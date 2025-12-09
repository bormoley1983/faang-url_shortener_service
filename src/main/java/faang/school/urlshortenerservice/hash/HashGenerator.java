package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final ExecutorService executor = Executors.newFixedThreadPool(200);
    private static final Integer  BATCH_SIZE_FOR_EXECUTOR = 2100;
    @Value("${hash.local-hash:1000}")
    private Integer numberOfLocalHash;

    @Value("${hash.generator.max-range:10000000}")
    private Integer maxRange;

    private final HashRepository hashRepository;

    @PostConstruct
    public void initGenerateHash() {
        hashGenerator();
    }

    @Transactional
    public List<Hash> getHash() {
        // todo продумать как получать случайные строки
        List<Hash> hashes = hashRepository.deleteAndReturnFirstN(numberOfLocalHash);

        if (hashes.size() < numberOfLocalHash) {
            hashGenerator();
        }

        log.info("generate hash for local hash! size - {}", hashes.size());
        return hashes;
    }

    // todo сохранять батчами
    @Transactional
    public void hashGenerator() {
        List<Long> listNumbers = hashRepository.getNextRange(maxRange);
        List<Hash> hashes = generateHashByBase62(listNumbers);

        log.info("generate hash! size - {}", hashes.size());
        hashRepository.saveAll(hashes);
    }

    private List<Hash> generateHashByBase62(List<Long> listNumbers) {
        Long a = System.nanoTime();

        List<List<Long>> batches = createBatches(listNumbers);

        List<CompletableFuture<List<Hash>>> batchFutures = createCompletableFuture(batches);

        List<Hash> hashes = waitingCompletableFuture(batchFutures);

        //323070101 - батчами, где батчи идут в поток
        //3102957900 - свои потоки, но каждый элемент listNumbers в поток
        //562720500 - последовательная операция
        //1764834100 - через @Async
        log.info("time for generate table hash {}  {}", System.nanoTime() - a, hashes.size());
        return hashes;
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

    private  <T> List<List<T>> createBatches(List<T> list) {
        int batchSize = BATCH_SIZE_FOR_EXECUTOR;
        return IntStream.range(0, (list.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> list.subList(i * batchSize, Math.min((i + 1) * batchSize, list.size())))
                .collect(Collectors.toList());
    }

    private List<CompletableFuture<List<Hash>>> createCompletableFuture(List<List<Long>> batches) {
        return batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() ->
                                batch.stream()
                                        .map(number -> new Hash(encodeBase62(number)))
                                        .collect(Collectors.toList())
                        , executor))
                .collect(Collectors.toList());
    }

    private List<Hash> waitingCompletableFuture(List<CompletableFuture<List<Hash>>> batchFutures) {
        return batchFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
