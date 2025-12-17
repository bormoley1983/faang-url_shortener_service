package faang.school.urlshortenerservice;

import faang.school.urlshortenerservice.base62encoder.Base62Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashGeneratorTest {

    @Mock
    private HashRepository hashRepository;

    @Mock
    private Base62Encoder base62Encoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private HashGenerator hashGenerator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hashGenerator, "batchSize", 3);
    }

    @Test
    void generateBatch_shouldGenerateEncodeAndSaveHashes() {
        List<Long> numbers = List.of(1L, 2L, 3L);
        List<String> encoded = List.of("a", "b", "c");

        when(hashRepository.getUniqueNumbers(3)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenReturn(encoded);

        when(jdbcTemplate.execute(any(ConnectionCallback.class)))
                .thenReturn(new int[]{1, 1, 1});

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(3);
        verify(base62Encoder).encode(numbers);
        verify(jdbcTemplate, atLeastOnce())
                .execute(any(ConnectionCallback.class));
    }

    @Test
    void saveHashesInBatches_shouldSplitIntoBatches() {
        List<Hash> hashes = List.of(
                hash("a"), hash("b"), hash("c"),
                hash("d"), hash("e")
        );

        when(jdbcTemplate.execute(any(ConnectionCallback.class)))
                .thenReturn(new int[]{1, 1, 1});

        hashGenerator.saveHashesInBatches(hashes);

        // batchSize = 3 -> 2 батча
        verify(jdbcTemplate, times(2))
                .execute(any(ConnectionCallback.class));
    }

    @Test
    void saveSingleBatch_shouldCountOnlySuccessfulInserts() {
        List<Hash> hashes = List.of(
                hash("a"), hash("b"), hash("c")
        );

        when(jdbcTemplate.execute(any(ConnectionCallback.class)))
                .thenReturn(new int[]{1, 0, 1});

        hashGenerator.saveHashesInBatches(hashes);

        verify(jdbcTemplate).execute(any(ConnectionCallback.class));
    }

    private Hash hash(String value) {
        Hash h = new Hash();
        h.setHash(value);
        return h;
    }
}
