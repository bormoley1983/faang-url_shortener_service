package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.apache.commons.collections4.ListUtils.partition;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {

    @Value("${hash.generator.batch-size.save-bd:500}")
    private Integer batchSizeForSaveBd;

    @Value("${hash.generator.hashes-in-bd.minimum}")
    private Integer minimumHashesInDd;

    @Value("${hash.local.count.hash:1000}")
    private Integer numberOfLocalHash;

    @Value("${hash.generator.max-range:10000000}")
    private Integer maxRange;

    @Value("${hash.generator.count.pool-executor:50}")
    private Integer countThreadExecutor;

    private final JdbcTemplate jdbcTemplate;
    private final HashRepository hashRepository;
    private final Base62Encode base62Encode;

    @Transactional
    public void checkCountHashInBd() {
        Long count = hashRepository.countTotal();
        if (count <= minimumHashesInDd) {
            log.info("hashes in bd have {},it's not enough! launch hash generator!", count);
            hashGenerator();
        }
        log.info("hashes in bd have {},that's enough!", count);
    }

    @Transactional
    public List<Hash> getHash() {
        List<Hash> hashes = hashRepository.deleteAndReturnFirstN(numberOfLocalHash);

        log.info("generate hash for local hash! size - {}", hashes.size());
        return hashes;
    }

    @Transactional
    public void hashGenerator() {
        hashGenerator(maxRange);
    }

    @Transactional
    public void hashGenerator(int count) {
        List<Long> listNumbers = hashRepository.getNextRange(count);

        log.info("Generating {} hashes", listNumbers.size());

        List<Hash> hashes = base62Encode.generateHashByBase62(listNumbers);
        List<Hash> mutable = new ArrayList<>(hashes);
        Collections.shuffle(mutable, ThreadLocalRandom.current());

        saveHashesInBatches(mutable);
    }

    public void saveHashesInBatches(List<Hash> hashes) {
        long startTime = System.nanoTime();
        int totalSaved = 0;

        List<List<Hash>> batches = partition(hashes, batchSizeForSaveBd);

        for (List<Hash> batch : batches) {
            int savedInBatch = saveSingleBatch(batch);
            totalSaved += savedInBatch;
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        log.info("Total batch insert time: {} ms for {} records ({} saved)",
                duration, hashes.size(), totalSaved);
    }

    private int saveSingleBatch(List<Hash> batch) {
        if (batch.isEmpty()) {
            return 0;
        }

        String sql = "INSERT INTO hash (hash) VALUES (?) ON CONFLICT (hash) DO NOTHING";

        int[] results = jdbcTemplate.batchUpdate(
                sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, batch.get(i).getHash());
                    }
                    @Override
                    public int getBatchSize() {
                        return batch.size();
                    }
                }
        );

        return (int) Arrays.stream(results).filter(result -> result > 0).count();
    }
}
