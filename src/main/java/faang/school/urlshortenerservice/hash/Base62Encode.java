package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class Base62Encode {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();

    @Value("${hash.generator.batch-size.executor:2100}")
    private Integer batchSizeForExecutor;

    private final ExecutorService executor = Executors.newFixedThreadPool(50);

    public List<Hash> generateHashByBase62(List<Long> listNumbers) {

        List<List<Long>> batches = createBatches(listNumbers);

        List<CompletableFuture<List<Hash>>> batchFutures = createCompletableFuture(batches);

        List<Hash> hashes = waitingCompletableFuture(batchFutures);

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
        int batchSize = batchSizeForExecutor;
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
