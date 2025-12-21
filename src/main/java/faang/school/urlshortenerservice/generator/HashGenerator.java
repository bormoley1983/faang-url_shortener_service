package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.base62encoder.Base62Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    private final JdbcTemplate jdbcTemplate;

    @Qualifier("hashCacheExecutor")
    private final ExecutorService hashCacheExecutor;

    @Value("${url-shortener.hash-generator.batch-size:100}")
    private int batchSize;

    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        log.info("HashGenerator started batch generation");

        List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);
        if (numbers.isEmpty()) {
            log.warn("No unique numbers available, generating synchronously");
            numbers = generateNumbersSynchronously(batchSize);
        }

        List<String> hashes = base62Encoder.encode(numbers);

        List<Hash> entities = hashes.stream().map(Hash::new).toList();

        try {
            int totalSaved = saveHashesInBatches(entities);
            log.info("HashGenerator finished batch generation, generated {} hashes, saved {}",
                    hashes.size(), totalSaved);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error during parallel batch insert", e);
            Thread.currentThread().interrupt();
        }
    }

    private List<Long> generateNumbersSynchronously(int count) {

        return LongStream.range(System.currentTimeMillis(), System.currentTimeMillis() + count)
                .boxed()
                .toList();
    }

    public int saveHashesInBatches(List<Hash> hashes) throws InterruptedException, ExecutionException {
        long startTime = System.nanoTime();
        int totalSaved = 0;

        List<List<Hash>> batches = partition(hashes, batchSize);
        List<Future<Integer>> futures = new ArrayList<>();

        for (List<Hash> batch : batches) {
            futures.add(hashCacheExecutor.submit(() -> saveSingleBatch(batch)));
        }

        for (Future<Integer> future : futures) {
            totalSaved += future.get();
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Parallel batch insert time: {} ms for {} records ({} saved)",
                duration, hashes.size(), totalSaved);

        return totalSaved;
    }

    public int saveSingleBatch(List<Hash> batch) {

        String sql = "INSERT INTO hash(hash) VALUES (?) ON CONFLICT DO NOTHING";

        int[] results = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, batch.get(i).getHash());
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });

        return (int) Arrays.stream(results).filter(r -> r == 1).count();
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return partitions;
    }
}
