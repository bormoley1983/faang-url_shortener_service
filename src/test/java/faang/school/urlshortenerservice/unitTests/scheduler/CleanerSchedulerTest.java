package faang.school.urlshortenerservice.unitTests.scheduler;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.scheduler.CleanerScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Тестирование CleanerScheduler")
@ExtendWith(MockitoExtension.class)
class CleanerSchedulerTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private HashRepository hashRepository;

    @InjectMocks
    private CleanerScheduler cleanerScheduler;

    private final int testPeriod = 2;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cleanerScheduler, "periodCleanUp", testPeriod);
    }

    @Test
    void shouldDeleteOldHashesAndSaveFree() {
        List<String> expectedFreeHashes = List.of("hash1", "hash2", "hash3");
        when(urlRepository.deleteOldHashesAndReturn(testPeriod)).thenReturn(expectedFreeHashes);

        cleanerScheduler.cleanUpUrl();

        verify(urlRepository, times(1)).deleteOldHashesAndReturn(testPeriod);
        verify(hashRepository, times(1)).saveAll(expectedFreeHashes);
        verifyNoMoreInteractions(urlRepository, hashRepository);
    }

    @Test
    @DisplayName("Should not call saveAll when list is empty")
    void shouldHandleEmptyList() {
        List<String> emptyList = List.of();
        when(urlRepository.deleteOldHashesAndReturn(testPeriod)).thenReturn(emptyList);

        cleanerScheduler.cleanUpUrl();

        verify(urlRepository, times(1)).deleteOldHashesAndReturn(testPeriod);
        verify(hashRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldHandleSingleHash() {
        List<String> singleHash = List.of("singleHash123");
        when(urlRepository.deleteOldHashesAndReturn(testPeriod)).thenReturn(singleHash);

        cleanerScheduler.cleanUpUrl();

        verify(urlRepository, times(1)).deleteOldHashesAndReturn(testPeriod);
        verify(hashRepository, times(1)).saveAll(singleHash);
    }

    @Test
    void shouldHandleRepositoryException() {
        when(urlRepository.deleteOldHashesAndReturn(testPeriod))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> cleanerScheduler.cleanUpUrl());
        verify(urlRepository, times(1)).deleteOldHashesAndReturn(testPeriod);
        verifyNoInteractions(hashRepository);
    }

    @Test
    void shouldHandleNullReturnFromRepository() {
        when(urlRepository.deleteOldHashesAndReturn(testPeriod)).thenReturn(List.of());

        cleanerScheduler.cleanUpUrl();

        verify(urlRepository, times(1)).deleteOldHashesAndReturn(testPeriod);
        verifyNoInteractions(hashRepository);
    }

}