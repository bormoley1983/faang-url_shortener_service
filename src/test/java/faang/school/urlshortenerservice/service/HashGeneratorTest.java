package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.HashRepositoryJdbc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashGeneratorTest {

    @Mock
    private Base62Encoder base62Encoder;

    @Mock
    private HashRepositoryJdbc hashRepository;

    @InjectMocks
    private HashGenerator hashGenerator;

    @Captor
    private ArgumentCaptor<List<Hash>> hashListCaptor;

    private static final int BATCH_SIZE = 1000;
    private static final List<Long> UNIQUE_NUMBERS = Arrays.asList(1L, 2L, 3L);
    private static final List<Hash> HASHES = Arrays.asList(
            new Hash("hash1"),
            new Hash("hash2"),
            new Hash("hash3")
    );
    private static final List<String> HASH_STRINGS = Arrays.asList("hash1", "hash2", "hash3");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hashGenerator, "batchSize", BATCH_SIZE);
    }

    @Test
    void testGenerateBatch() {
        when(hashRepository.getUniqueNumbers(BATCH_SIZE)).thenReturn(UNIQUE_NUMBERS);
        when(base62Encoder.encodeTheList(UNIQUE_NUMBERS)).thenReturn(HASH_STRINGS);
        when(hashRepository.saveAll(anyList())).thenReturn(HASHES.size());

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(BATCH_SIZE);
        verify(base62Encoder).encodeTheList(UNIQUE_NUMBERS);
        verify(hashRepository).saveAll(hashListCaptor.capture());

        List<Hash> savedHashes = hashListCaptor.getValue();
        assertEquals(HASHES.get(1).getHash(), savedHashes.get(1).getHash());
    }

    @Test
    void testGenerateBatchWhenNoUniqueNumbers() {
        when(hashRepository.getUniqueNumbers(BATCH_SIZE)).thenReturn(List.of());
        when(base62Encoder.encodeTheList(List.of())).thenReturn(List.of());
        when(hashRepository.saveAll(anyList())).thenReturn(0);

        hashGenerator.generateBatch();

        verify(hashRepository).getUniqueNumbers(BATCH_SIZE);
        verify(base62Encoder).encodeTheList(List.of());
        verify(hashRepository).saveAll(argThat(List::isEmpty));
    }

    @Test
    void testGenerateBatchWhenSaveFails() {
        when(hashRepository.getUniqueNumbers(BATCH_SIZE)).thenReturn(UNIQUE_NUMBERS);
        when(base62Encoder.encodeTheList(UNIQUE_NUMBERS)).thenReturn(HASH_STRINGS);
        when(hashRepository.saveAll(anyList())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> hashGenerator.generateBatch());
    }

    @Test
    void testGetHashesWhenEnoughHashesInRepository() {
        int amount = 2;
        when(hashRepository.getAndDeleteHashBatch(amount)).thenReturn(HASHES.subList(0, amount));

        List<String> result = hashGenerator.getHashes(amount);

        assertEquals(HASH_STRINGS.subList(0, amount), result);
        verify(hashRepository).getAndDeleteHashBatch(amount);
        verify(hashRepository, never()).getUniqueNumbers(anyInt());
        verify(base62Encoder, never()).encodeTheList(anyList());
    }

    @Test
    void testGetHashesWithZeroAmount() {
        List<String> result = hashGenerator.getHashes(0);

        assertTrue(result.isEmpty());
        verify(hashRepository).getAndDeleteHashBatch(anyInt());
    }

    @Test
    void testGetHashesWithNegativeAmount() {
        List<String> result = hashGenerator.getHashes(-1);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateBatchWhenCalledByScheduler() {
        when(hashRepository.getUniqueNumbers(BATCH_SIZE)).thenReturn(UNIQUE_NUMBERS);
        when(base62Encoder.encodeTheList(UNIQUE_NUMBERS)).thenReturn(HASH_STRINGS);
        when(hashRepository.saveAll(anyList())).thenReturn(HASHES.size());

        hashGenerator.generateBatch();

        verify(hashRepository).saveAll(anyList());
    }
}