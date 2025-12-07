package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HashCacheImplTest {

    private static final int CAPACITY = 1000;
    private static final int FILL_PERCENT = 20;

    @Mock
    private HashGenerator hashGenerator;

    @Mock
    private HashRepository hashRepository;

    @Mock
    private ExecutorService executorService;

    private HashCacheImpl hashCache;

    @BeforeEach
    void setUp() {
        hashCache = new HashCacheImpl(hashGenerator, hashRepository, executorService);
        ReflectionTestUtils.setField(hashCache, "capacity", CAPACITY);
        ReflectionTestUtils.setField(hashCache, "fillPercent", FILL_PERCENT);

        when(executorService.submit(org.mockito.ArgumentMatchers.any(Runnable.class)))
                .thenAnswer(invocation -> {
                    Runnable task = invocation.getArgument(0);
                    task.run();
                    return null;
                });

        when(hashRepository.getHashBatch()).thenReturn(List.of());
    }

    @Test
    void getHash_ShouldThrowExceptionWhenCacheIsEmpty() {
        hashCache.init();

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> hashCache.getHash()
        );
    }

    @Test
    void init_ShouldCallGenerateBatch() {
        hashCache.init();
        verify(hashGenerator).generateBatch();
    }
}