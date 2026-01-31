package faang.school.urlshortenerservice.utils;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.Base62Encoder;
import faang.school.urlshortenerservice.util.HashGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashGeneratorTest {

    @Mock
    private HashRepository hashRepository;

    @Mock
    private Base62Encoder base62Encoder;

    @InjectMocks
    private HashGenerator hashGenerator;

    @Captor
    private ArgumentCaptor<List<Hash>> hashesCaptor;

    @Test
    void generateBatch_shouldCreateAndSaveHashes_usingEncodeFixed() {
        int batchSize = 3;
        ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

        List<Long> numbers = List.of(1L, 2L, 3L);
        List<String> hashes = List.of("a", "b", "c");

        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encodeFixed(numbers)).thenReturn(hashes);

        hashGenerator.generateBatch();
        verify(hashRepository).getUniqueNumbers(batchSize);
        verify(base62Encoder).encodeFixed(numbers);
        verify(hashRepository).saveAll(hashesCaptor.capture());

        List<Hash> savedHashes = hashesCaptor.getValue();
        assertEquals(3, savedHashes.size());
        for (int i = 0; i < savedHashes.size(); i++) {
            assertEquals(hashes.get(i), savedHashes.get(i).getHash());
        }
    }
    
    // @Test
    // void generateBatch_shouldCreateAndSaveHashes() {
    //     int batchSize = 3;
    //     ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

    //     List<Long> numbers = List.of(1L, 2L, 3L);
    //     List<String> hashes = List.of("a", "b", "c");

    //     when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
    //     when(base62Encoder.encode(numbers)).thenReturn(hashes);
    //     //List<Hash> hashEntities = hashes.stream().map(Hash::new).collect(Collectors.toList());

    //     hashGenerator.generateBatch();
    //     verify(hashRepository).getUniqueNumbers(batchSize);
    //     verify(base62Encoder).encode(numbers);
    //     verify(hashRepository).saveAll(hashesCaptor.capture());

    //     List<Hash> savedHashes = hashesCaptor.getValue();
    //     assertEquals(3, savedHashes.size());
    //     for (int i = 0; i < savedHashes.size(); i++) {
    //         assertEquals(hashes.get(i), savedHashes.get(i).getHash());
    //     }
    // }

    @Test
    void generateBatch_whenNoNumbersFound_shouldThrowException() {
        int batchSize = 5;
        ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(List.of());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            hashGenerator.generateBatch();
        });

        assertEquals("There are no free Numbers for generating new hashes!", exception.getMessage());
        verify(hashRepository).getUniqueNumbers(batchSize);
    }

    @Test
    void generateBatch_shouldUseCorrectBatchSize_withEncodeFixed() {
        int batchSize = 10;
        ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

        List<Long> numbers = List.of(1L);
        List<String> hashes = List.of("a");

        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encodeFixed(numbers)).thenReturn(hashes);

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(batchSize);
        verify(base62Encoder).encodeFixed(numbers);
        verify(hashRepository).saveAll(hashesCaptor.capture());
        List<Hash> savedHashes = hashesCaptor.getValue();
        assertEquals(1, savedHashes.size());
        assertEquals("a", savedHashes.get(0).getHash());
    }

    // @Test
    // void generateBatch_shouldUseCorrectBatchSize() {
    //     int batchSize = 10;
    //     ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

    //     List<Long> numbers = List.of(1L);
    //     List<String> hashes = List.of("a");

    //     when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
    //     when(base62Encoder.encode(numbers)).thenReturn(hashes);

    //     hashGenerator.generateBatch();

    //     verify(hashRepository).getUniqueNumbers(batchSize);
    // }

        @Test
    void generateBatch_shouldLogErrorWhenNoNumbersFound() {
        int batchSize = 2;
        ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(List.of());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> hashGenerator.generateBatch());
        assertEquals("There are no free Numbers for generating new hashes!", exception.getMessage());
    }

    @Test
    void generateBatch_shouldNotSaveHashesIfEncodeFixedReturnsEmpty() {
        int batchSize = 2;
        ReflectionTestUtils.setField(hashGenerator, "batchSize", batchSize);

        List<Long> numbers = List.of(1L, 2L);
        List<String> hashes = List.of();

        when(hashRepository.getUniqueNumbers(batchSize)).thenReturn(numbers);
        when(base62Encoder.encodeFixed(numbers)).thenReturn(hashes);

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(batchSize);
        verify(base62Encoder).encodeFixed(numbers);
        verify(hashRepository).saveAll(hashesCaptor.capture());
        List<Hash> savedHashes = hashesCaptor.getValue();
        assertEquals(0, savedHashes.size());
    }
}
