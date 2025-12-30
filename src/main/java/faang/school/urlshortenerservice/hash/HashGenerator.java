package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.repo.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final SqidsEncoder sqidsEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final HashStorageProperties properties;

    private static final String SQL = """
            INSERT INTO hash (hash)
            VALUES (?)
            ON CONFLICT DO NOTHING
            """;

    @Transactional
    public void generateHash() {
        List<Long> numbers = hashRepository.getUniqueNumbers(properties.getRangeUniqueNumbers());
        List<String> hashes = sqidsEncoder.encode(numbers);

        jdbcTemplate.batchUpdate(
                SQL,
                hashes,
                properties.getBatchSizeGeneratedHashes(),
                (ps, hash) -> ps.setString(1, hash)
        );
    }
}
