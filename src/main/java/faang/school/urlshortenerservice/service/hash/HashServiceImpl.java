package faang.school.urlshortenerservice.service.hash;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.service.async.AsyncServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class HashServiceImpl implements HashService {
    private final HashGenerator hashGenerator;
    private final JdbcTemplate jdbcTemplate;
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @PostConstruct
    @Transactional
    public void init(){
        this.generateHash();
    }

    @Transactional
    @Override
    public void generateHash() {
        List<String> hashes = hashGenerator.generateHash();
        saveHashByBatch(hashes);
    }

    @Override
    public List<String> getHashes(long hashLimit) {
        return hashGenerator.getHashes(hashLimit);
    }

    @Transactional
    @Override
    public void saveHashByBatch(List<String> hashes) {
        long start = System.currentTimeMillis();
        log.info("Starting saving hash");
        String sql = "INSERT INTO hash (hash) VALUES (?)";
        for (int i = 0; i < hashes.size(); i += batchSize) {
            int end = Math.min(i + batchSize, hashes.size());
            List<String> batch = hashes.subList(i, end);

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
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