package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.repository.HashDao;
import faang.school.urlshortenerservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HashCacheServiceTest {
    private HashDao hashDao;
    private HashGenerator hashGenerator;
    private Executor executor;
    private HashCacheService hashCacheService;

    @BeforeEach
    void setUp() {
        hashDao = mock(HashDao.class);
        hashGenerator = mock(HashGenerator.class);
        executor = Executors.newSingleThreadExecutor();

        hashCacheService = new HashCacheService(hashDao, hashGenerator, executor);
        TestUtils.setField(hashCacheService, "maxCacheSize", 100);
        TestUtils.setField(hashCacheService, "reloadThreshold", 0.5);
        TestUtils.setField(hashCacheService, "reloadBatchSize", 50);
    }

    @Test
    void testGetHashWhenCacheHasHash() {
        TestUtils.offerHashToCache(hashCacheService, "abc123");
        String hash = hashCacheService.getHash();
        assertEquals("abc123", hash);
    }

    @Test
    void testGetHashWhenCacheEmptyButRefillSucceeds() {
        when(hashDao.getHashBatch(anyInt())).thenReturn(List.of("xyz789"));
        String hash = hashCacheService.getHash();
        assertEquals("xyz789", hash);
    }

    @Test
    void testGetHashWhenRefillFails() {
        when(hashDao.getHashBatch(anyInt())).thenReturn(List.of());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            hashCacheService.getHash();
        });
        assertEquals("No available hashes after emergency refill.", ex.getMessage());
    }

    @Test
    void testWarmUpWhenRefillSucceeds() {
        when(hashDao.getHashBatch(anyInt())).thenReturn(List.of("a", "b", "c"));
        assertDoesNotThrow(() -> hashCacheService.warmUpOnStartup());
    }

    @Test
    void testWarmUpWhenRefillFails_throwsException() {
        when(hashDao.getHashBatch(anyInt())).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> hashCacheService.warmUpOnStartup());
    }
}
