package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class SaveBdService {

    private final JdbcTemplate jdbcTemplate;


    public int saveSingleBatch(List<Hash> batch) {
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
