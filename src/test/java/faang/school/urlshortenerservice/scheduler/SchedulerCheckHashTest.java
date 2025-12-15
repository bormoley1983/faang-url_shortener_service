package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.service.url.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerCheckHashTest {

    @Mock
    private UrlService urlService;

    @Mock
    private HashGenerator hashGenerator;

    @InjectMocks
    private SchedulerCheckHash schedulerCheckHash;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(schedulerCheckHash, "hashCapacity", 1000L);
        ReflectionTestUtils.setField(schedulerCheckHash, "minCapacityPercent", 20);
    }

    @Test
    void checkCountHash_WhenCapacityBelowMinPercent_ShouldGenerateHashes() {
        long countInDb = 150L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator).generateHash();
    }

    @Test
    void checkCountHash_WhenCapacityExactlyMinPercent_ShouldNotGenerateHashes() {
        long countInDb = 200L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator, never()).generateHash();
    }

    @Test
    void checkCountHash_WhenCapacityAboveMinPercent_ShouldNotGenerateHashes() {
        long countInDb = 300L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator, never()).generateHash();
    }

    @Test
    void checkCountHash_WhenDatabaseEmpty_ShouldGenerateHashes() {
        long countInDb = 0L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator).generateHash();
    }

    @Test
    void checkCountHash_WhenDatabaseAlmostFull_ShouldNotGenerateHashes() {
        long countInDb = 800L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator, never()).generateHash();
    }

    @Test
    void checkCountHash_WhenExactlyOneHashLessThanMinPercent_ShouldGenerateHashes() {
        long countInDb = 199L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator).generateHash();
    }

    @Test
    void checkCountHash_WithDifferentMinCapacityPercent_ShouldWorkCorrectly() {
        ReflectionTestUtils.setField(schedulerCheckHash, "minCapacityPercent", 10);

        long countInDb = 80L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator).generateHash();
    }

    @Test
    void checkCountHash_WithDifferentHashCapacity_ShouldCalculateCorrectly() {
        ReflectionTestUtils.setField(schedulerCheckHash, "hashCapacity", 500L);

        long countInDb = 99L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator).generateHash();
    }

    @Test
    void checkCountHash_WithZeroHashCapacity_ShouldHandleGracefully() {
        ReflectionTestUtils.setField(schedulerCheckHash, "hashCapacity", 0L);

        long countInDb = 0L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();

        verify(hashGenerator, never()).generateHash();
    }

    @Test
    void checkCountHash_VerifyLogMessages_ShouldLogAppropriately() {
        long countInDb = 150L;
        when(urlService.countHashRepository()).thenReturn(countInDb);

        schedulerCheckHash.checkCountHash();

        verify(urlService).countHashRepository();
        verify(hashGenerator).generateHash();
    }
}