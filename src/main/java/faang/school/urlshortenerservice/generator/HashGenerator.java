package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.base62encoder.Base62Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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

        String sql = "INSERT INTO hash (hash) VALUES (?) ON CONFLICT (hash) DO NOTHING";

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

        int inserted = 0;
        for (int result : results) {
            if (result > 0) inserted++;
        }

        return inserted;
    }
}
