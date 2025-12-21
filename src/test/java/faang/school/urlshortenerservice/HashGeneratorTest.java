package faang.school.urlshortenerservice;

import faang.school.urlshortenerservice.base62encoder.Base62Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1, 1, 1});

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(3);
        verify(base62Encoder).encode(numbers);
        verify(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void generateBatch_shouldDoNothingIfNoNumbersAvailable() {
        when(hashRepository.getUniqueNumbers(3)).thenReturn(List.of());

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(3);
        verifyNoInteractions(base62Encoder);
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    void generateSingleHashSynchronously_shouldGenerateAndSaveHash() {
        List<Long> numbers = List.of(42L);
        List<String> encoded = List.of("abc");

        when(hashRepository.getUniqueNumbers(1)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenReturn(encoded);
        when(jdbcTemplate.batchUpdate(anyString(), any(BatchPreparedStatementSetter.class)))
                .thenReturn(new int[]{1});

        String result = hashGenerator.generateSingleHashSynchronously();

        Assertions.assertEquals("abc", result);
        verify(jdbcTemplate).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void generateSingleHashSynchronously_shouldFailIfNoNumbers() {
        when(hashRepository.getUniqueNumbers(1)).thenReturn(List.of());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> hashGenerator.generateSingleHashSynchronously()
        );
    }
}
