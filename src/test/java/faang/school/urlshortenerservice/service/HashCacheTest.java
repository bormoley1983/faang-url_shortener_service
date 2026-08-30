package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.HashGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HashCacheTest {
    @Mock
    private HashRepository hashRepository;

    @Mock
    private HashPoolService hashPoolService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private HashGenerator hashGenerator;

    @Mock
    private DataSource dataSource;

    private HashCache hashCache;

    @BeforeEach
    void setUp() {
        hashCache = new HashCache(hashRepository, hashPoolService, executorService, hashGenerator, dataSource);

        ReflectionTestUtils.setField(hashCache, "maxCacheSize", 100);
        ReflectionTestUtils.setField(hashCache, "thresholdPercent", 1);
        ReflectionTestUtils.setField(hashCache, "batchSize", 50);
        ReflectionTestUtils.setField(hashCache, "dbThreshold", 10);
        ReflectionTestUtils.setField(hashCache, "refillWaitTimeoutMs", 5L);

        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(executorService).execute(any(Runnable.class));
    }

    @Test
    void getHash_whenCacheBelowThreshold_shouldRefillCache() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.add("hash1");
        ReflectionTestUtils.setField(hashCache, "hashQueue", queue);
        ReflectionTestUtils.setField(hashCache, "isRefilling", new AtomicBoolean(false));
        ReflectionTestUtils.setField(hashCache, "thresholdPercent", 20);

        when(hashPoolService.takeBatch(50)).thenReturn(Arrays.asList("AAA001", "AAA002", "AAA003"));
        when(hashRepository.countAvailableHashes()).thenReturn(100);

        String hash = hashCache.getNextHash();

        assertEquals("hash1", hash);
        verify(executorService).execute(any(Runnable.class));
        verify(hashPoolService).takeBatch(50);
        verify(hashGenerator, never()).generateBatch();
    }

    @Test
    void getHashCache_shouldReturnMultipleHashes() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.add("hash1");
        queue.add("hash2");
        queue.add("hash3");
        ReflectionTestUtils.setField(hashCache, "hashQueue", queue);

        String first = hashCache.getNextHash();
        String second = hashCache.getNextHash();
        String third = hashCache.getNextHash();

        assertEquals("hash1", first);
        assertEquals("hash2", second);
        assertEquals("hash3", third);
    }

    @Test
    void getNextHash_shouldReturnAfterTimeout_whenAnotherRefillIsStuck() {
        ReflectionTestUtils.setField(hashCache, "isRefilling", new AtomicBoolean(true));

        assertNull(hashCache.getNextHash());
    }
}
