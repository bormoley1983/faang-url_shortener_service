package faang.school.urlshortenerservice.service.hash;

import faang.school.urlshortenerservice.config.hash.HashCacheProperties;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.service.HashCacheImpl;
import faang.school.urlshortenerservice.service.HashGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HashCacheImpl unit tests (mock-based, no DB)")
class HashCacheImplTest {

    @Mock
    HashRepository hashRepository;
    @Mock
    HashGenerator hashGenerator;

    private ExecutorService realExecutor;

    @AfterEach
    void tearDown() {
        if (realExecutor != null) {
            realExecutor.shutdownNow();
        }
    }

    @Test
    @DisplayName("getHash returns first cached element and does not trigger refill when cache is above threshold")
    void getHash_returnsFromCache_whenCacheAboveThreshold() throws Exception {
        HashCacheProperties props = props(10, 20, 5); // threshold = 2
        ExecutorService executor = mock(ExecutorService.class);

        HashCacheImpl cache = new HashCacheImpl(props, hashRepository, hashGenerator, executor);
        BlockingQueue<String> q = internalQueue(cache);

        q.offer("A");
        q.offer("B");
        q.offer("C"); // currentSize = 3 >= threshold

        String result = cache.getHash();

        assertThat(result).isEqualTo("A");
        verify(executor, never()).submit(any(Runnable.class));
        verifyNoInteractions(hashRepository);
    }

    @Test
    @DisplayName("getHash triggers async refill when cache is below threshold")
    void getHash_triggersRefill_whenCacheLow() throws Exception {
        HashCacheProperties props = props(10, 20, 7);
        ExecutorService executor = mock(ExecutorService.class);

        when(executor.submit(any(Runnable.class))).thenAnswer(inv -> {
            inv.<Runnable>getArgument(0).run();
            return CompletableFuture.completedFuture(null);
        });

        when(hashRepository.getHashBatch(anyInt())).thenReturn(List.of("H1", "H2", "H3"));
        when(hashGenerator.generateBatch()).thenReturn(CompletableFuture.completedFuture(3));

        HashCacheImpl cache = new HashCacheImpl(props, hashRepository, hashGenerator, executor);

        cache.getHash();

        verify(executor, times(1)).submit(any(Runnable.class));
        verify(hashRepository, times(1)).getHashBatch(anyInt());
        verify(hashGenerator, times(1)).generateBatch();
        assertThat(internalQueue(cache)).isNotEmpty();
    }

    @Test
    @DisplayName("refill is exclusive: concurrent callers do not start multiple refills")
    void refill_isExclusive_underConcurrency() {
        realExecutor = Executors.newSingleThreadExecutor();

        CountDownLatch refillStarted = new CountDownLatch(1);
        CountDownLatch allowFinish = new CountDownLatch(1);

        when(hashRepository.getHashBatch(anyInt())).thenAnswer(inv -> {
            refillStarted.countDown();
            allowFinish.await(2, TimeUnit.SECONDS);
            return List.of("X1", "X2");
        });

        when(hashGenerator.generateBatch()).thenReturn(CompletableFuture.completedFuture(2));
        HashCacheProperties props = props(100, 20, 10);
        HashCacheImpl cache = new HashCacheImpl(props, hashRepository, hashGenerator, realExecutor);

        int callers = 20;
        ExecutorService callersPool = Executors.newFixedThreadPool(callers);
        try {
            for (int i = 0; i < callers; i++) {
                callersPool.submit(cache::getHash);
            }

            assertThat(refillStarted.await(2, TimeUnit.SECONDS)).isTrue();
            verify(hashRepository, timeout(200).times(1)).getHashBatch(anyInt());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assertions.fail(e);
        } finally {
            allowFinish.countDown();
            callersPool.shutdownNow();
        }

        verify(hashRepository, timeout(1000).times(1)).getHashBatch(anyInt());
    }

    @Test
    @DisplayName("refill respects remaining queue capacity and requests only what can be cached")
    void refill_respectsRemainingCapacity() throws Exception {
        HashCacheProperties props = props(5, 80, 10); // threshold = 4
        ExecutorService executor = mock(ExecutorService.class);

        when(executor.submit(any(Runnable.class))).thenAnswer(inv -> {
            inv.<Runnable>getArgument(0).run();
            return CompletableFuture.completedFuture(null);
        });

        when(hashGenerator.generateBatch()).thenReturn(CompletableFuture.completedFuture(0));

        HashCacheImpl cache = new HashCacheImpl(props, hashRepository, hashGenerator, executor);
        BlockingQueue<String> q = internalQueue(cache);

        q.offer("A");
        q.offer("B");
        q.offer("C"); // remainingCapacity = 2, currentSize = 3 < threshold

        when(hashRepository.getHashBatch(eq(2))).thenReturn(List.of("N1", "N2", "N3"));

        cache.getHash();

        verify(hashRepository).getHashBatch(3);
        assertThat(q.size()).isLessThanOrEqualTo(5);
    }

    private static HashCacheProperties props(int capacity, int thresholdPercent, int refillBatchSize) {
        HashCacheProperties p = new HashCacheProperties();
        p.setCapacity(capacity);
        p.setRefillThresholdPercent(thresholdPercent);
        p.setRefillBatchSize(refillBatchSize);
        return p;
    }

    @SuppressWarnings("unchecked")
    private static BlockingQueue<String> internalQueue(HashCacheImpl cache) throws Exception {
        Field f = HashCacheImpl.class.getDeclaredField("cache");
        f.setAccessible(true);
        return (BlockingQueue<String>) f.get(cache);
    }
}