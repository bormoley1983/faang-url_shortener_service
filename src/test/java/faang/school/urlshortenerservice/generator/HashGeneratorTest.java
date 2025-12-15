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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HashGeneratorTest {

    @Mock
    private HashRepository hashRepository;

    @Mock
    private Base62Encoder base62Encoder;

    @InjectMocks
    private HashGenerator hashGenerator;

    private static final int MAX_RANGE = 100;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hashGenerator, "maxRange", MAX_RANGE);
    }

    @Test
    void generateHash_successfullyGeneratesAndReturnsHashes() {
        List<Long> numbers = List.of(1000L, 1001L, 1002L);
        when(hashRepository.getNextRange(MAX_RANGE)).thenReturn(numbers);
        when(base62Encoder.encodeToBase62(1000L)).thenReturn("q0");
        when(base62Encoder.encodeToBase62(1001L)).thenReturn("q1");
        when(base62Encoder.encodeToBase62(1002L)).thenReturn("q2");

        List<String> result = hashGenerator.generateHash();

        assertThat(result)
                .hasSize(3)
                .containsExactly("q0", "q1", "q2");

        verify(hashRepository).getNextRange(MAX_RANGE);
        verify(base62Encoder).encodeToBase62(1000L);
        verify(base62Encoder).encodeToBase62(1001L);
        verify(base62Encoder).encodeToBase62(1002L);
        verifyNoMoreInteractions(hashRepository, base62Encoder);
    }

    @Test
    void generateHash_emptyRange_throwsGenerateHashesException() {
        when(hashRepository.getNextRange(MAX_RANGE)).thenReturn(List.of());

        assertThatThrownBy(() -> hashGenerator.generateHash())
                .isInstanceOf(GenerateHashesException.class)
                .hasMessage("Error with generate hash is empty");

        verify(hashRepository).getNextRange(MAX_RANGE);
        verifyNoInteractions(base62Encoder);
    }

    @Test
    void getHashes_whenEnoughHashesInRepository_returnsThemWithoutGeneration() {
        long requestedCount = 5;
        List<Hash> availableHashes = List.of(
                new Hash("hash1"), new Hash("hash2"), new Hash("hash3"),
                new Hash("hash4"), new Hash("hash5")
        );
        when(hashRepository.findAndDelete(requestedCount)).thenReturn(availableHashes);

        List<String> result = hashGenerator.getHashes(requestedCount);

        assertThat(result)
                .hasSize(5)
                .containsExactly("hash1", "hash2", "hash3", "hash4", "hash5");

        verify(hashRepository).findAndDelete(requestedCount);
        verifyNoInteractions(base62Encoder);
        verify(hashRepository, never()).getNextRange(anyInt());
    }

    @Test
    void getHashes_whenNotEnoughHashes_generatesMoreAndCompletesTheList() {
        long requestedCount = 7;

        List<Hash> initialHashes = List.of(new Hash("old1"), new Hash("old2"), new Hash("old3"));
        when(hashRepository.findAndDelete(requestedCount)).thenReturn(initialHashes);

        List<Long> newNumbers = List.of(2000L, 2001L, 2002L, 2003L);
        when(hashRepository.getNextRange(MAX_RANGE)).thenReturn(newNumbers);
        when(base62Encoder.encodeToBase62(2000L)).thenReturn("newA");
        when(base62Encoder.encodeToBase62(2001L)).thenReturn("newB");
        when(base62Encoder.encodeToBase62(2002L)).thenReturn("newC");
        when(base62Encoder.encodeToBase62(2003L)).thenReturn("newD");

        List<Hash> newHashes = List.of(
                new Hash("newA"), new Hash("newB"), new Hash("newC"), new Hash("newD")
        );
        when(hashRepository.findAndDelete(4L)).thenReturn(newHashes);

        List<String> result = hashGenerator.getHashes(requestedCount);

        assertThat(result)
                .hasSize(7)
                .containsExactly("old1", "old2", "old3", "newA", "newB", "newC", "newD");

        verify(hashRepository).findAndDelete(requestedCount);
        verify(hashRepository).getNextRange(MAX_RANGE);
        verify(hashRepository).findAndDelete(4L);
        verify(base62Encoder, times(4)).encodeToBase62(anyLong());
    }

    @Test
    void getHashes_whenZeroHashesRequested_returnsEmptyList() {
        List<String> result = hashGenerator.getHashes(0);

        assertThat(result).isEmpty();
        verify(hashRepository).findAndDelete(0L);
        verifyNoMoreInteractions(hashRepository);
        verifyNoInteractions(base62Encoder);
    }
}