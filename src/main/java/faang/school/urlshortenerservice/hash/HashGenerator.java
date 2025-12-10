package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.logging.LoggingRebinder;
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

    // todo вынести все волшебные переменные в yaml
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final ExecutorService executor = Executors.newFixedThreadPool(200);
    private static final Integer  BATCH_SIZE_FOR_EXECUTOR = 2100;
    private static final int BATCH_SIZE_FOR_SAVE_BD = 500;

    @Value("${hash.generator.hashes-in-bd.minimum}")
    private Integer minimumHashesInDd;

    @Value("${hash.local-hash:1000}")
    private Integer numberOfLocalHash;

    @Value("${hash.generator.max-range:10000000}")
    private Integer maxRange;

    private final JdbcTemplate jdbcTemplate;
    private final HashRepository hashRepository;


    @PostConstruct
    public void initGenerateHash() {
        hashGenerator();
    }

    @Transactional
    public void checkCountHashInBd() {
        Long a = System.nanoTime();
        Long count = hashRepository.countTotal();
        if (count <= minimumHashesInDd) {
            log.info("hashes in bd have {},it's not enough! launch hash generator!", count);
            hashGenerator();
        }
        log.info("hashes in bd have {},that's enough!", count);
        log.info("Time check DB {}", System.nanoTime() - a);
    }

    @Transactional
    public List<Hash> getHash() {
        // todo продумать как получать случайные строки
        List<Hash> hashes = hashRepository.deleteAndReturnFirstN(numberOfLocalHash);

        if (hashes.size() < numberOfLocalHash) {
            int needed = numberOfLocalHash - hashes.size();
            hashGenerator(Math.max(needed, maxRange));

            List<Hash> additionalHashes = hashRepository.deleteAndReturnFirstN(needed);
            hashes.addAll(additionalHashes);
        }

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

        List<Hash> hashes = generateHashByBase62(listNumbers);

        saveHashesInBatches(hashes);
    }

    private void saveHashesInBatches(List<Hash> hashes) {
        long startTime = System.nanoTime();
        int totalSaved = 0;

        List<List<Hash>> batches = partition(hashes, BATCH_SIZE_FOR_SAVE_BD);

        for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
            List<Hash> batch = batches.get(batchIndex);
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

   // private void saveHashesWithJdbcTemplate(List<Hash> hashes) {
   //     Long startTime = System.nanoTime();
//
   //     String sql = "INSERT INTO hash (hash) VALUES (?) ON CONFLICT DO NOTHING";
//
   //     jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
   //         @Override
   //         public void setValues(PreparedStatement ps, int i) throws SQLException {
   //             ps.setString(1, hashes.get(i).getHash());
   //         }
//
   //         @Override
   //         public int getBatchSize() {
   //             return BATCH_SIZE_FOR_SAVE_BD;
   //         }
   //     });
//
   //     log.info("Batch insert time: {} ms for {} records",
   //             (System.nanoTime() - startTime) / 1_000_000, hashes.size());
   // }


    private List<Hash> generateHashByBase62(List<Long> listNumbers) {

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
