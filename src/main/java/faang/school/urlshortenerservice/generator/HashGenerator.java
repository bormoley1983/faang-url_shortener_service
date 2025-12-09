package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.exception.GenerateHashesException;
import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    private final JdbcTemplate jdbcTemplate;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @Value(value = "${generator.maxRange}")
    private int maxRange;

    @Transactional
    public void generateHash() {
        log.debug("Starting generate hash");
        List<Long> range = hashRepository.getNextRange(maxRange);
        log.debug("Obtained values from the database {}", range.size());
        List<String> hashes = range.stream()
                .map(base62Encoder::encodeToBase62)
                .toList();
        if (hashes.isEmpty()) {
            throw new GenerateHashesException("Error with generate hash is empty");
        }
        log.info("Starting saving hash ");
        saveHashByBatch(hashes);
    }

    @Transactional
    public List<String> getHashes(long hashLimit) {
        List<Hash> hashes = hashRepository.findAndDelete(hashLimit);
        if (hashes.size() < hashLimit) {
            generateHash();
            hashes.addAll(hashRepository.findAndDelete(hashLimit - hashes.size()));
        }
        return hashes.stream()
                .map(Hash::getHash)
                .toList();
    }

    @Async(value = "threadExecutor")
    public CompletableFuture<List<String>> getHashesAsync(long amount) {
        return CompletableFuture.completedFuture(getHashes(amount));
    }

    @Transactional
    public void saveHashByBatch(List<String> hashes) {
        long start = System.currentTimeMillis();

        String sql = "INSERT INTO hash (hash) VALUES (?)";
        for (int i = 0; i < hashes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, hashes.size());
            List<String> batch = hashes.subList(i, end);

            jdbcTemplate.batchUpdate(sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int idx) throws SQLException {
                            ps.setString(1, batch.get(idx));
                        }

                        @Override
                        public int getBatchSize() {
                            return batch.size();
                        }
                    }
            );
        }
        long time = System.currentTimeMillis() - start;
        log.info("All hash saved: {} hashes in {} ms, number of batch save iterations {}",
                hashes.size(), time, hashes.size() / batchSize);
    }
}