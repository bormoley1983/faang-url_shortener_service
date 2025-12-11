package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.exception.GenerateHashesException;
import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
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
        ReflectionTestUtils.setField(hashGenerator, "batchSize", 1000);
        ReflectionTestUtils.setField(hashGenerator, "maxRange", 100);
    }

    @Test
    void generateHash_success() {
        when(hashRepository.getNextRange(100)).thenReturn(List.of(100L, 101L, 102L));
        when(base62Encoder.encodeToBase62(100L)).thenReturn("bM");
        when(base62Encoder.encodeToBase62(101L)).thenReturn("bN");
        when(base62Encoder.encodeToBase62(102L)).thenReturn("bO");

        hashGenerator.generateHash();

        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void generateHash_emptyRange_throwsException() {
        when(hashRepository.getNextRange(100)).thenReturn(List.of());

        assertThatThrownBy(() -> hashGenerator.generateHash())
                .isInstanceOf(GenerateHashesException.class)
                .hasMessage("Error with generate hash is empty");
    }

    @Test
    void getHashes_enoughInRepo_returnsFromRepo() {
        List<Hash> hashes = List.of(
                new Hash("a"), new Hash("b"), new Hash("c"), new Hash("d"), new Hash("e")
        );
        when(hashRepository.findAndDelete(5)).thenReturn(hashes);

        List<String> result = hashGenerator.getHashes(5);

        assertThat(result).containsExactly("a", "b", "c", "d", "e");
        verifyNoInteractions(base62Encoder, jdbcTemplate);
        verify(hashRepository, never()).getNextRange(anyInt());
    }

    @Test
    void getHashesAsync_returnsCompletedFuture() {
        List<Hash> firstBatch = new ArrayList<>();
        firstBatch.add(new Hash("a"));
        firstBatch.add(new Hash("b"));

        List<Hash> secondBatch = new ArrayList<>();
        secondBatch.add(new Hash("c"));
        secondBatch.add(new Hash("d"));
        secondBatch.add(new Hash("e"));
        secondBatch.add(new Hash("f"));
        secondBatch.add(new Hash("g"));
        secondBatch.add(new Hash("h"));
        secondBatch.add(new Hash("i"));
        secondBatch.add(new Hash("j"));

        when(hashRepository.findAndDelete(10))
                .thenReturn(firstBatch);
        when(hashRepository.findAndDelete(8))
                .thenReturn(secondBatch);

        when(hashRepository.getNextRange(100))
                .thenReturn(List.of(1001L, 1002L, 1003L, 1004L, 1005L, 1006L, 1007L, 1008L));

        when(base62Encoder.encodeToBase62(1001L)).thenReturn("c");
        when(base62Encoder.encodeToBase62(1002L)).thenReturn("d");
        when(base62Encoder.encodeToBase62(1003L)).thenReturn("e");
        when(base62Encoder.encodeToBase62(1004L)).thenReturn("f");
        when(base62Encoder.encodeToBase62(1005L)).thenReturn("g");
        when(base62Encoder.encodeToBase62(1006L)).thenReturn("h");
        when(base62Encoder.encodeToBase62(1007L)).thenReturn("i");
        when(base62Encoder.encodeToBase62(1008L)).thenReturn("j");

        CompletableFuture<List<String>> future = hashGenerator.getHashesAsync(10);

        List<String> expected = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        assertThat(future).isCompletedWithValue(expected);
    }

    @Test
    void saveHashByBatch_splitsIntoBatches() {
        List<String> hashes = List.of("a1", "a2", "a3", "a4", "a5", "a6", "a7");

        ReflectionTestUtils.setField(hashGenerator, "batchSize", 3);

        hashGenerator.saveHashByBatch(hashes);

        verify(jdbcTemplate, times(3)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }
}