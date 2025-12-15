package faang.school.urlshortenerservice.service;

import java.util.concurrent.CompletableFuture;

/**
 * Generates a new batch of hashes and persists them to DB.
 * Intended to be executed asynchronously (non-blocking for callers).
 */
public interface HashGenerator {

    /**
     * Generates and saves a batch of hashes.
     *
     * @return future with the number of successfully saved hashes
     */
    CompletableFuture<Integer> generateBatch();
}