package faang.school.urlshortenerservice.service.async;

import faang.school.urlshortenerservice.service.hash.HashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncServiceImplTest {

    @Mock
    private HashService hashService;

    @InjectMocks
    private AsyncServiceImpl asyncService;

    @Test
    void getHashesAsync_validAmount_returnsCompletableFutureWithHashes() {
        long amount = 100L;
        List<String> expectedHashes = List.of("hash1", "hash2", "hash3");
        when(hashService.getHashes(amount)).thenReturn(expectedHashes);

        CompletableFuture<List<String>> future = asyncService.getHashesAsync(amount);
        List<String> result;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedHashes, result);
        verify(hashService).getHashes(amount);
    }
}