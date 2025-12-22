package faang.school.urlshortenerservice.unit.service.hash;

import faang.school.urlshortenerservice.config.hash.HashProperties;
import faang.school.urlshortenerservice.repository.db.HashRepository;
import faang.school.urlshortenerservice.service.Base62Encoder;
import faang.school.urlshortenerservice.service.HashGeneratorImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HashGeneratorImpl unit tests (mock-based, no DB)")
class HashGeneratorImplTest {

    @Mock
    private HashRepository hashRepository;
    @Mock
    private HashProperties hashProperties;
    @Mock
    private Base62Encoder base62Encoder;

    @InjectMocks
    private HashGeneratorImpl hashGenerator;

    @Test
    @DisplayName("generateBatch: happy path — get numbers, encode, save, return hashes count")
    void generateBatch_shouldGetNumbers_encode_save_andReturnCount() {
        int batchSize = 5;
        List<Long> numbers = List.of(1L, 2L, 3L, 4L, 5L);
        List<String> hashes = List.of("000001", "000002", "000003", "000004", "000005");

        when(hashProperties.getBatchSize()).thenReturn(batchSize);
        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenReturn(hashes);

        CompletableFuture<Integer> future = hashGenerator.generateBatch();

        assertThat(future.join()).isEqualTo(hashes.size());

        InOrder inOrder = inOrder(hashProperties, hashRepository, base62Encoder);
        inOrder.verify(hashProperties).getBatchSize();
        inOrder.verify(hashRepository).getUniqueNumbers(batchSize);
        inOrder.verify(base62Encoder).encode(numbers);
        inOrder.verify(hashRepository).save(hashes);
    }

    @Test
    @DisplayName("generateBatch: repository returns less numbers than batchSize — still encode and save")
    void generateBatch_whenNumbersSizeLessThanBatchSize_shouldStillEncodeAndSave() {
        int batchSize = 5;
        List<Long> numbers = List.of(10L, 11L, 12L);
        List<String> hashes = List.of("00000A", "00000B", "00000C");

        when(hashProperties.getBatchSize()).thenReturn(batchSize);
        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenReturn(hashes);

        CompletableFuture<Integer> future = hashGenerator.generateBatch();

        assertThat(future.join()).isEqualTo(3);
        verify(hashRepository).save(hashes);
    }

    @Test
    @DisplayName("generateBatch: encoder returns different size — throw IllegalStateException and do not save")
    void generateBatch_whenEncoderReturnsSizeMismatch_shouldThrowAndNotSave() {
        int batchSize = 3;
        List<Long> numbers = List.of(1L, 2L, 3L);
        List<String> hashes = List.of("000001", "000002");

        when(hashProperties.getBatchSize()).thenReturn(batchSize);
        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenReturn(hashes);

        assertThatThrownBy(() -> hashGenerator.generateBatch())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Encoder returned size mismatch");

        verify(hashRepository, never()).save(any());
    }

    @Test
    @DisplayName("generateBatch: encoder throws exception — propagate exception and do not save")
    void generateBatch_whenEncoderThrows_shouldPropagateAndNotSave() {
        int batchSize = 2;
        List<Long> numbers = List.of(1L, 2L);

        when(hashProperties.getBatchSize()).thenReturn(batchSize);
        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> hashGenerator.generateBatch())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        verify(hashRepository, never()).save(any());
    }
}