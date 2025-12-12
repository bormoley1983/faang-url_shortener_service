package faang.school.urlshortenerservice.config;

import faang.school.urlshortenerservice.properties.RetryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryExecutor {

    private final RetryProperties retryProperties;

    public <T> T execute(Supplier<T> operation) {
        int maxAttempts = retryProperties.getMaxAttempts();
        long delayMs = retryProperties.getDelayMs();
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    log.warn("Operation failed (attempt {}/{}). Retrying in {} ms. Error: {}",
                            attempt, maxAttempts, delayMs, e.getMessage());
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    log.error("Operation failed after {} attempts", maxAttempts, e);
                }
            }
        }
        throw new RuntimeException("Operation failed after {} attempts", lastException);
    }

    public void execute(Runnable operation) {
        execute(() -> {
            operation.run();
            return null;
        });
    }
}
