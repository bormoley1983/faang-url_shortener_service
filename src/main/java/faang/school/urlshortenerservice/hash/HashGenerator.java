package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.repo.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base64Encoder base64Encoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${hash.storage.range:1000}")
    private int maxRange;

    @Value("${hash.storage.batch_size:100}")
    private int batchSize;

    private static final String SQL = """
            INSERT INTO hash (hash)
            VALUES (?)
            ON CONFLICT DO NOTHING
            """;

    @Transactional
    public void generateHash() {
        List<Long> numbers = hashRepository.getUniqueNumbers(maxRange);
        List<String> hashes = base64Encoder.encode(numbers);

        jdbcTemplate.batchUpdate(
                SQL,
                hashes,
                batchSize,
                (ps, hash) -> ps.setString(1, hash)
        );
    }
}
