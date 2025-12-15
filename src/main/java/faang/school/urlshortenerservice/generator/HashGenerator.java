package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.base62encoder.Base62Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections4.ListUtils.partition;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${url-shortener.hash-generator.batch-size:100}")
    private int batchSize;

    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        log.info("HashGenerator started batch generation");

        List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);

        List<String> hashes = base62Encoder.encode(numbers);

        List<Hash> entities = hashes.stream()
                .map(h -> {
                    Hash hash = new Hash();
                    hash.setHash(h);
                    return hash;
                })
                .toList();

        saveHashesInBatches(entities);

        log.info("HashGenerator finished batch generation, generated {} hashes", hashes.size());
    }

    public void saveHashesInBatches(List<Hash> hashes) {
        long startTime = System.nanoTime();
        int totalSaved = 0;

        List<List<Hash>> batches = partition(hashes, batchSize);

        for (List<Hash> batch : batches) {
            int savedInBatch = saveSingleBatch(batch);
            totalSaved += savedInBatch;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Total batch insert time: {} ms for {} records ({} saved)",
                duration, hashes.size(), totalSaved);
    }

    private int saveSingleBatch(List<Hash> batch) {

        final String sql = SqlQueries.INSERT_HASH;

        int[] results = jdbcTemplate.execute((Connection connection) -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (Hash h : batch) {
                    ps.setString(1, h.getHash());
                    ps.addBatch();
                }
                return ps.executeBatch();
            }
        });

        assert results != null;
        return (int) Arrays.stream(results)
                .filter(r -> r == 1)
                .count();
    }
}
