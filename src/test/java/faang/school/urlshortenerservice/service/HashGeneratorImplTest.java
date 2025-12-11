package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.encoder.Base62Encoder;
import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashGeneratorImplTest {

    private static final int BATCH_SIZE = 3;

    @Mock
    private Base62Encoder base62Encoder;

    @Mock
    private HashRepository hashRepository;

    @InjectMocks
    private HashGeneratorImpl hashGenerator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hashGenerator, "batchSize", BATCH_SIZE);
    }

    @Test
    void generateBatch_ShouldReturnHashesAndSaveThem() throws Exception {
        List<Long> numbers = List.of(1L, 2L, 3L);
        List<String> hashes = List.of("a", "b", "c");

        when(hashRepository.getUniqueNumbers(BATCH_SIZE)).thenReturn(numbers);
        when(base62Encoder.encode(numbers)).thenReturn(hashes);

        CompletableFuture<List<String>> future = hashGenerator.generateBatch();
        List<String> result = future.get(); // метод и так возвращает completedFuture

        assertThat(result).isEqualTo(hashes);
        verify(hashRepository).getUniqueNumbers(BATCH_SIZE);
        verify(base62Encoder).encode(numbers);
        verify(hashRepository).save(hashes);
    }

    @Test
    void generateBatch_ShouldReturnEmptyList() throws Exception {
        when(hashRepository.getUniqueNumbers(BATCH_SIZE)).thenReturn(List.of());

        CompletableFuture<List<String>> future = hashGenerator.generateBatch();
        List<String> result = future.get();

        assertThat(result).isEmpty();
        verify(hashRepository).getUniqueNumbers(BATCH_SIZE);
        verifyNoInteractions(base62Encoder);
        verify(hashRepository, never()).save(anyList());
    }
}
