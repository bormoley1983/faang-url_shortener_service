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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${url-shortener.hash-generator.batch-size:1000}")
    private int batchSize;

    @Async("hashGeneratorExecutor")
    public void generateBatch() {
        try {
            List<Long> numbers = hashRepository.getUniqueNumbers(batchSize);
            if (numbers.isEmpty()) {
                log.warn("No unique numbers available for async batch");
                return;
            }

            List<Hash> hashes = base62Encoder.encode(numbers)
                    .stream()
                    .map(Hash::new)
                    .toList();

            int saved = saveBatch(hashes);

            log.info("HashGenerator async batch finished: generated={}, saved={}",
                    hashes.size(), saved);

        } catch (Exception e) {
            log.error("HashGenerator async batch failed", e);
        }
    }

    public String generateSingleHashSynchronously() {
        List<Long> numbers = hashRepository.getUniqueNumbers(1);
        if (numbers.isEmpty()) {
            throw new IllegalStateException("No unique numbers available for sync generation");
        }

        String hash = base62Encoder.encode(numbers).get(0);
        saveBatch(List.of(new Hash(hash)));

        return hash;
    }

    private int saveBatch(List<Hash> batch) {
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
}
