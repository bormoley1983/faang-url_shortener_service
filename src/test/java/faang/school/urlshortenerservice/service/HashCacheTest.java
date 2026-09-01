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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

    @Test
    void init_shouldScheduleAsyncWarmUp_whenPoolEmptyAndDbBelowThreshold() throws Exception {
        when(hashPoolService.takeBatch(50)).thenReturn(List.of());
        when(hashRepository.countAvailableHashes()).thenReturn(5);
        mockLockAcquired();

        hashCache.init();

        // Warm-up runs inline via the mock executor; generation happens for empty pool and low DB count.
        verify(executorService).execute(any(Runnable.class));
        verify(hashGenerator, org.mockito.Mockito.atLeastOnce()).generateBatch();
    }

    @Test
    void init_shouldSkipGeneration_whenAnotherInstanceHoldsLock() throws Exception {
        when(hashPoolService.takeBatch(50)).thenReturn(List.of());
        when(hashRepository.countAvailableHashes()).thenReturn(5);
        mockLockNotAcquired();

        hashCache.init();

        verify(executorService).execute(any(Runnable.class));
        verify(hashGenerator, never()).generateBatch();
    }

    @Test
    void init_shouldNotBlockStartup_whenDatabaseUnavailable() throws Exception {
        // DB down during warm-up: the async refill swallows the failure so context init is not blocked.
        lenient().when(hashPoolService.takeBatch(50)).thenReturn(List.of());
        lenient().when(hashRepository.countAvailableHashes()).thenReturn(5);
        when(dataSource.getConnection()).thenThrow(new java.sql.SQLException("db down"));

        hashCache.init();

        verify(executorService).execute(any(Runnable.class));
    }

    @Test
    void init_shouldNotBlockStartup_whenExecutorRejectsWarmUp() {
        doThrow(new RejectedExecutionException("pool full"))
                .when(executorService).execute(any(Runnable.class));

        // Must not throw — startup proceeds and the first request refills synchronously.
        hashCache.init();

        verify(executorService).execute(any(Runnable.class));
    }

    @Test
    void getNextHash_shouldRefillSync_whenQueueEmpty() {
        when(hashPoolService.takeBatch(50)).thenReturn(List.of("AAA001"));
        when(hashRepository.countAvailableHashes()).thenReturn(100);

        String hash = hashCache.getNextHash();

        assertEquals("AAA001", hash);
    }

    @Test
    void getNextHash_shouldReturnNull_whenRefillYieldsNoHashes() throws Exception {
        when(hashPoolService.takeBatch(50)).thenReturn(List.of());
        when(hashRepository.countAvailableHashes()).thenReturn(100);
        mockLockAcquired();

        assertNull(hashCache.getNextHash());
    }

    @Test
    void getNextHash_shouldCompleteRefill_whenExecutorRejectsTask() throws Exception {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        ReflectionTestUtils.setField(hashCache, "hashQueue", queue);
        doThrow(new RejectedExecutionException("pool full"))
                .when(executorService).execute(any(Runnable.class));
        when(hashPoolService.takeBatch(50)).thenReturn(List.of());
        when(hashRepository.countAvailableHashes()).thenReturn(100);
        mockLockAcquired();

        assertNull(hashCache.getNextHash());
    }

    @Test
    void getNextHash_shouldSwallowRefillError_whenAsyncRefillFails() {
        // Pre-populate queue so checkAndRefillIfNeeded triggers async refill (size < threshold)
        // but poll() succeeds, avoiding the sync refill path that would propagate the error.
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.add("hash1");
        ReflectionTestUtils.setField(hashCache, "hashQueue", queue);
        ReflectionTestUtils.setField(hashCache, "thresholdPercent", 20); // threshold = 20

        // Async refill runs inline via mock executor; takeBatch failure is swallowed by refillCacheQuietly
        when(hashPoolService.takeBatch(50)).thenThrow(new RuntimeException("pool down"));

        String hash = hashCache.getNextHash();

        assertEquals("hash1", hash);
        verify(executorService).execute(any(Runnable.class));
    }

    @Test
    void getNextHash_shouldReturnNull_whenSyncRefillFails() {
        // empty queue forces a synchronous refill; takeBatch failure propagates from refillCacheInternal
        when(hashPoolService.takeBatch(50)).thenThrow(new RuntimeException("pool down"));

        assertThrows(RuntimeException.class, hashCache::getNextHash);
    }

    @Test
    void getNextHash_shouldWaitForConcurrentRefill_thenReturnHash() throws Exception {
        // Simulate another thread holding the refill flag; release it shortly.
        ReflectionTestUtils.setField(hashCache, "isRefilling", new AtomicBoolean(true));
        ReflectionTestUtils.setField(hashCache, "refillWaitTimeoutMs", 200L);

        Object monitor = ReflectionTestUtils.getField(hashCache, "refillMonitor");
        Thread releaser = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
            queue.add("AAA001");
            ReflectionTestUtils.setField(hashCache, "hashQueue", queue);
            synchronized (monitor) {
                ReflectionTestUtils.setField(hashCache, "isRefilling", new AtomicBoolean(false));
                monitor.notifyAll();
            }
        });
        releaser.start();

        String hash = hashCache.getNextHash();

        releaser.join(1000);
        assertEquals("AAA001", hash);
    }

    private void mockLockAcquired() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(1)).thenReturn(true);
    }

    private void mockLockNotAcquired() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(1)).thenReturn(false);
    }
}
