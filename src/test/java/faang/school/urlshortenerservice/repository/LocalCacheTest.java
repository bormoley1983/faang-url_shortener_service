package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.cache.LocalCache;
import faang.school.urlshortenerservice.service.async.AsyncService;
import faang.school.urlshortenerservice.service.hash.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalCacheTest {

    @Mock
    private HashService hashService;

    @Mock
    private AsyncService asyncService;

    @InjectMocks
    private LocalCache localCache;

    private static final int CAPACITY = 100;
    private static final int MIN_CAPACITY_PERCENT = 20;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(localCache, "capacity", CAPACITY);
        ReflectionTestUtils.setField(localCache, "minCapacityPercent", MIN_CAPACITY_PERCENT);
    }

    @Test
    void testInit_Success() {
        List<String> mockHashes = List.of("hash1", "hash2", "hash3");
        when(hashService.getHashes(CAPACITY)).thenReturn(mockHashes);

        localCache.init();

        verify(hashService, times(1)).getHashes(CAPACITY);
        assertEquals(3, getHashQueueSize());
    }

    @Test
    void testGetHash_ReturnsHashWhenQueueHasElements() {
        List<String> mockHashes = List.of("hash1", "hash2");
        when(hashService.getHashes(CAPACITY)).thenReturn(mockHashes);
        localCache.init();

        String result = localCache.getHash();

        assertNotNull(result);
        assertTrue(mockHashes.contains(result));
        assertEquals(1, getHashQueueSize());
    }

    @Test
    void testGetHash_ReturnsNullWhenQueueIsEmpty() {
        when(hashService.getHashes(CAPACITY)).thenReturn(List.of());
        localCache.init();

        String result = localCache.getHash();

        assertNull(result);
    }

    @Test
    void testGetHash_DoesNotTriggerAsyncRefillWhenAboveMinCapacity() {
        List<String> initialHashes = generateHashes(30); // 30%
        when(hashService.getHashes(CAPACITY)).thenReturn(initialHashes);
        localCache.init();

        for (int i = 0; i < 5; i++) {
            localCache.getHash();
        }

        verify(asyncService, never()).getHashesAsync(anyInt());
    }

    @Test
    void testCheckCapacity_ReturnsTrueWhenBelowMin() {
        List<String> initialHashes = generateHashes(15);
        when(hashService.getHashes(CAPACITY)).thenReturn(initialHashes);
        localCache.init();

        assertTrue(invokeCheckCapacity());
    }

    @Test
    void testCheckCapacity_ReturnsFalseWhenAboveMin() {
        List<String> initialHashes = generateHashes(25);
        when(hashService.getHashes(CAPACITY)).thenReturn(initialHashes);
        localCache.init();

        assertFalse(invokeCheckCapacity());
    }

    @Test
    void testCheckCapacity_ReturnsTrueWhenExactlyAtMin() {
        List<String> initialHashes = generateHashes(20);
        when(hashService.getHashes(CAPACITY)).thenReturn(initialHashes);
        localCache.init();

        assertFalse(invokeCheckCapacity());
    }

    private int getHashQueueSize() {
        return ((ArrayBlockingQueue<String>) ReflectionTestUtils.getField(localCache, "hashLocal")).size();
    }

    private boolean invokeCheckCapacity() {
        return ReflectionTestUtils.invokeMethod(localCache, "checkCapacity");
    }

    private List<String> generateHashes(int percent) {
        int count = CAPACITY * percent / 100;
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "hash" + i)
                .toList();
    }
}