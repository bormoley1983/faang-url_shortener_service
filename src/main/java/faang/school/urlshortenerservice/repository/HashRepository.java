package faang.school.urlshortenerservice.repository;

import java.util.List;

/**
 * Repository for hash-related DB operations:
 * - obtain unique numbers (via a DB sequence) for hash generation
 * - store generated hashes
 * - fetch a batch of currently free hashes for in-memory cache
 */
public interface HashRepository {

    /**
     * Gets N unique numbers from DB (typically from a sequence) used as input for hash generation.
     */
    List<Long> getUniqueNumbers(int n);

    /**
     * Persists generated hashes into DB.
     * Usually implemented with batch insert + ON CONFLICT DO NOTHING to avoid duplicates.
     */
    void save(List<String> hashes);

    /**
     * Fetches a batch of free hashes (not yet used in url table) for caching.
     *
     * @param batchSize number of hashes to fetch
     */
    List<String> getHashBatch(int batchSize);
}