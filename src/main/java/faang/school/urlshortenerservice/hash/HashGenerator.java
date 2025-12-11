package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.collections4.ListUtils.partition;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {


    // todo в БД хешей 10_000_000
    // todo в локал хеш 10_000
    // todo в бачсайз минимут 10_000
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
        // todo решить проблему 3 бекендов
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
        // todo нужна ли потом будет?
     //   if (hashes.size() < numberOfLocalHash) {
     //       int needed = numberOfLocalHash - hashes.size();
     //       hashGenerator(Math.max(needed, maxRange));
//
     //       List<Hash> additionalHashes = hashRepository.deleteAndReturnFirstN(needed);
     //       hashes.addAll(additionalHashes);
     //   }

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

        saveHashesInBatches(hashes);
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
        // todo нужен PrepareStatment
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
