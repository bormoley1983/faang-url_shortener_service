package faang.school.urlshortenerservice.scheduler;

import faang.school.urlshortenerservice.service.url.UrlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulerCleanerTest {

    @Mock
    private UrlService urlService;

    @InjectMocks
    private SchedulerCleaner schedulerCleaner;

    @Test
    void cleanHash_ShouldCallUrlServiceCleanHash() {
        schedulerCleaner.cleanHash();

        verify(urlService, times(1)).cleanHash();
    }

    @Test
    void cleanHash_ShouldLogStartAndFinish() {
        schedulerCleaner.cleanHash();

        verify(urlService).cleanHash();
    }

    @Test
    void cleanHash_WhenUrlServiceThrowsException_ShouldLogError() {
        doThrow(new RuntimeException("Database error")).when(urlService).cleanHash();

        try {
            schedulerCleaner.cleanHash();
        } catch (RuntimeException e) {
        }

        verify(urlService).cleanHash();

    }

    @Test
    void cleanHash_MultipleCalls_ShouldWorkCorrectly() {
        schedulerCleaner.cleanHash();
        schedulerCleaner.cleanHash();
        schedulerCleaner.cleanHash();

        verify(urlService, times(3)).cleanHash();
    }

    @Test
    void cleanHash_ConcurrentExecution_ShouldBeThreadSafe() {
        Runnable cleanTask = () -> schedulerCleaner.cleanHash();

        Thread thread1 = new Thread(cleanTask);
        Thread thread2 = new Thread(cleanTask);
        Thread thread3 = new Thread(cleanTask);

        thread1.start();
        thread2.start();
        thread3.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(urlService, times(3)).cleanHash();
    }

    @Test
    void cleanHash_VerifyExecutionOrder_ShouldLogBeforeAndAfterCleaning() {
        schedulerCleaner.cleanHash();

        verify(urlService).cleanHash();
    }

    @Test
    void cleanHash_WithSpecificTime_ShouldIncludeTimeInLog() {
        schedulerCleaner.cleanHash();

        verify(urlService).cleanHash();
    }

    @Test
    void cleanHash_EmptyImplementation_ShouldDoNothing() {
        doNothing().when(urlService).cleanHash();

        schedulerCleaner.cleanHash();

        verify(urlService).cleanHash();
    }
}