package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.generator.HashGenerator;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalCacheTest {

    @Mock
    private HashGenerator hashGenerator;

    @InjectMocks
    private LocalCache localCache;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(localCache, "capacity", 1000);
        ReflectionTestUtils.setField(localCache, "minCapacityPercent", 20);
    }

    @Test
    public void init_shouldFillQueueWithHashes() {
        List<String> hashes = List.of("hash1", "hash2", "hash3", "hash4", "hash5");
        when(hashGenerator.getHashes(1000)).thenReturn(hashes);

        localCache.init();

        verify(hashGenerator).getHashes(1000);
        assertThat(localCache.getHash()).isIn(hashes);
    }

    @Test
    public void getHash_whenQueueIsEmpty_shouldReturnNull() {
        when(hashGenerator.getHashes(1000)).thenReturn(List.of());
        localCache.init();

        String hash = localCache.getHash();

        assertThat(hash).isNull();
    }

    @Test
    public void getHash_whenCapacityAboveMinPercent_shouldNotTriggerRefill() {
        List<String> initialHashes = generateHashes(250);
        when(hashGenerator.getHashes(1000)).thenReturn(initialHashes);

        localCache.init();

        String hash = localCache.getHash();

        assertThat(hash).isNotNull();
        verify(hashGenerator, never()).getHashesAsync(anyInt());
    }

    @Test
    public void getHash_whenAlreadyFilling_shouldNotStartAnotherRefill() {
        List<String> initialHashes = generateHashes(150);
        when(hashGenerator.getHashes(1000)).thenReturn(initialHashes);

        CompletableFuture<List<String>> future = new CompletableFuture<>();
        when(hashGenerator.getHashesAsync(1000)).thenReturn(future);

        localCache.init();

        localCache.getHash();
        localCache.getHash();

        verify(hashGenerator, times(1)).getHashesAsync(1000);
    }


    @Test
    public void getHash_withMinCapacityPercentZero_shouldNeverRefill() {
        ReflectionTestUtils.setField(localCache, "minCapacityPercent", 0);
        List<String> initialHashes = generateHashes(10);
        when(hashGenerator.getHashes(1000)).thenReturn(initialHashes);
        localCache.init();

        String hash = localCache.getHash();
        assertThat(hash).isNotNull();

        verify(hashGenerator, never()).getHashesAsync(anyInt());
    }

    @Test
    public void getHash_multiThreadedAccess_shouldBeThreadSafe() throws InterruptedException {
        List<String> initialHashes = generateHashes(500);
        when(hashGenerator.getHashes(1000)).thenReturn(initialHashes);

        CompletableFuture<List<String>> future = CompletableFuture.completedFuture(generateHashes(1000));
        when(hashGenerator.getHashesAsync(1000)).thenReturn(future);

        localCache.init();

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    localCache.getHash();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        verify(hashGenerator, atLeastOnce()).getHashesAsync(1000);
    }

    private List<String> generateHashes(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "hash" + i)
                .toList();
    }
}