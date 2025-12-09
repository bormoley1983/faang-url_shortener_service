package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.model.Hash;
import faang.school.urlshortenerservice.repository.UrlHashJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashGeneratorTest {

    @Mock
    private UrlHashJdbcRepository repository;

    @InjectMocks
    private HashGenerator hashGenerator;

    @BeforeEach
    void setUp() throws Exception {
        repository = mock(UrlHashJdbcRepository.class);
        hashGenerator = new HashGenerator(repository);

        var field = HashGenerator.class.getDeclaredField("rangeSequence");
        field.setAccessible(true);
        field.set(hashGenerator, 5);
    }

    @Test
    void testBase62Encoding() throws Exception {
        var method = HashGenerator.class.getDeclaredMethod("base62Encoding", long.class);
        method.setAccessible(true);

        assertEquals("0", method.invoke(hashGenerator, 0L));
        assertEquals("1", method.invoke(hashGenerator, 1L));
        assertEquals("a", method.invoke(hashGenerator, 36L));
        assertEquals("z", method.invoke(hashGenerator, 61L));
        assertEquals("10", method.invoke(hashGenerator, 62L));
    }

    @Test
    void testGetShuffledBase62() throws Exception {
        var method = HashGenerator.class.getDeclaredMethod("getShuffledBase62", long.class);
        method.setAccessible(true);

        String hash1 = (String) method.invoke(hashGenerator, 123L);
        String hash2 = (String) method.invoke(hashGenerator, 456L);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2);
        assertEquals(6, hash1.length());
        assertEquals(6, hash2.length());
    }

    @Test
    void testGenerateHash_callsRepository() {
        when(repository.getNextRangeSequence(anyInt())).thenReturn(List.of(1L, 2L, 3L));

        hashGenerator.generateHash();

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository, times(1)).batchInsertHashes(captor.capture());

        List<String> insertedHashes = captor.getValue();
        assertEquals(3, insertedHashes.size());
    }

    @Test
    void testGetHashes_enoughHashes() {
        Hash h1 = new Hash(); h1.setHash("aaa");
        Hash h2 = new Hash(); h2.setHash("bbb");

        when(repository.findAndDelete(2L)).thenReturn(List.of(h1, h2));

        List<String> result = hashGenerator.getHashes(2);

        assertEquals(2, result.size());
        assertTrue(result.contains("aaa"));
        assertTrue(result.contains("bbb"));
        verify(repository, never()).batchInsertHashes(any());
    }

    @Test
    void getHashes_notEnoughHashes_callsGenerateHash_returnsAll() {
        Hash h1 = new Hash(); h1.setHash("aaa");
        Hash h2 = new Hash(); h2.setHash("bbb");

        when(repository.findAndDelete(2L))
                .thenReturn(new ArrayList<>(List.of(h1)));

        when(repository.findAndDelete(1L))
                .thenReturn(new ArrayList<>(List.of(h2)));

        when(repository.getNextRangeSequence(5)).thenReturn(List.of(1L, 2L, 3L));

        List<String> result = hashGenerator.getHashes(2);

        assertEquals(2, result.size());
        assertTrue(result.contains("aaa"));
        assertTrue(result.contains("bbb"));

        verify(repository, times(1)).findAndDelete(2L);
        verify(repository, times(1)).findAndDelete(1L);
        verify(repository, times(1)).getNextRangeSequence(5);
        verify(repository, times(1)).batchInsertHashes(anyList());
    }

    @Test
    void testGetHashesAsync() throws Exception {
        Hash h1 = new Hash(); h1.setHash("aaa");
        when(repository.findAndDelete(1L)).thenReturn(List.of(h1));

        CompletableFuture<List<String>> future = hashGenerator.getHashesAsync(1);
        List<String> result = future.get();

        assertEquals(1, result.size());
        assertEquals("aaa", result.get(0));
    }
}