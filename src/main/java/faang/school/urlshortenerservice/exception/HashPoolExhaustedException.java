package faang.school.urlshortenerservice.exception;

import lombok.Getter;

/**
 * Thrown when the hash number pool has no free numbers left to generate new hashes.
 * <p>
 * This is a transient, retryable condition: callers can distinguish "pool temporarily
 * exhausted — retry later" from a permanent failure. It is unchecked so it propagates
 * through {@code @Transactional} boundaries and triggers rollback, while still carrying
 * domain semantics for error handlers and Kafka retry/DLT policies.
 */
@Getter
public class HashPoolExhaustedException extends RuntimeException {

    private final int batchSize;

    public HashPoolExhaustedException(int batchSize) {
        super("There are no free numbers available to generate a hash batch of size " + batchSize);
        this.batchSize = batchSize;
    }

    public HashPoolExhaustedException(String message, int batchSize, Throwable cause) {
        super(message, cause);
        this.batchSize = batchSize;
    }
}
