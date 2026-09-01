package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.util.HashGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Slf4j
@Service
public class HashCache {
    private static final String LOCK_SQL = "SELECT pg_try_advisory_lock(hashtext('hash_table_batch_operation')::bigint)";
    private static final String UNLOCK_SQL = "SELECT pg_advisory_unlock(hashtext('hash_table_batch_operation')::bigint)";

    private final HashRepository hashRepository;
    private final HashPoolService hashPoolService;
    private final ExecutorService executorService;
    private final HashGenerator hashGenerator;
    private final DataSource dataSource;

    private final ConcurrentLinkedQueue<String> hashQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);
    private final Object refillMonitor = new Object();

    @Value("${cache.hash.size:1000}")
    private int maxCacheSize;

    @Value("${cache.hash.threshold-percent:20}")
    private int thresholdPercent;

    @Value("${cache.hash.batch-size:500}")
    private int batchSize;

    @Value("${cache.hash.db-threshold:100}")
    private int dbThreshold;

    @Value("${cache.hash.refill-wait-timeout-ms:5000}")
    private long refillWaitTimeoutMs;

    @PostConstruct
    public void init() {
        // Warm up the cache asynchronously so a slow or unavailable database at startup
        // does not block Spring context initialization. The first getNextHash() call has a
        // synchronous fallback (refillCacheSync), so an async warm-up is safe.
        try {
            executorService.execute(this::refillCacheQuietly);
        } catch (RuntimeException ex) {
            log.warn("Could not schedule initial hash cache warm-up; first request will refill synchronously", ex);
        }
    }

    public String getNextHash() {
        checkAndRefillIfNeeded();
        String hash = hashQueue.poll();
        if (hash != null) {
            return hash;
        }

        refillCacheSync();
        return hashQueue.poll();
    }

    private void checkAndRefillIfNeeded() {
        int threshold = maxCacheSize * thresholdPercent / 100;
        if (hashQueue.size() < threshold && isRefilling.compareAndSet(false, true)) {
            log.info("Cache below threshold ({}%). Starting async refill.", thresholdPercent);
            try {
                executorService.execute(this::refillCacheQuietly);
            } catch (RuntimeException ex) {
                completeRefill();
                log.error("Failed to schedule hash cache refill", ex);
            }
        }
    }

    private void refillCacheSync() {
        if (isRefilling.compareAndSet(false, true)) {
            try {
                refillCacheInternal();
            } finally {
                completeRefill();
            }
            return;
        }

        waitForRefill();
    }

    private void refillCacheQuietly() {
        try {
            refillCacheInternal();
        } catch (Exception e) {
            log.error("Error during hash cache refill", e);
        } finally {
            completeRefill();
        }
    }

    private void refillCacheInternal() {
        log.info("Starting cache refill. Current size: {}", hashQueue.size());

        addHashesFromTable();
        if (hashQueue.isEmpty()) {
            generateHashPoolBatch();
            addHashesFromTable();
        }

        int dbCount = hashRepository.countAvailableHashes();
        if (dbCount < dbThreshold) {
            log.info("DB hash count ({}) is below threshold ({}).", dbCount, dbThreshold);
            generateHashPoolBatch();
        }

        log.info("Cache refill completed. New cache size: {}", hashQueue.size());
    }

    private void addHashesFromTable() {
        List<String> availableHashes = hashPoolService.takeBatch(batchSize);
        if (availableHashes != null && !availableHashes.isEmpty()) {
            availableHashes.forEach(hashQueue::offer);
            log.info("Refilled cache from hash pool with {} hashes.", availableHashes.size());
        } else {
            log.info("No available hashes in hash pool.");
        }
    }

    private void generateHashPoolBatch() {
        try (Connection connection = dataSource.getConnection()) {
            if (!tryAcquireLock(connection)) {
                log.info("Another instance is already generating new hashes.");
                return;
            }

            try {
                log.info("Distributed lock acquired. Generating new hash pool batch.");
                hashGenerator.generateBatch();
            } finally {
                releaseLock(connection);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to manage hash generation lock", ex);
        }
    }

    private boolean tryAcquireLock(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getBoolean(1);
        }
    }

    private void releaseLock(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(UNLOCK_SQL)) {
            statement.execute();
        } catch (SQLException ex) {
            log.error("Failed to release advisory lock", ex);
        }
    }

    private void waitForRefill() {
        long remainingNanos = TimeUnit.MILLISECONDS.toNanos(refillWaitTimeoutMs);
        long deadline = System.nanoTime() + remainingNanos;

        synchronized (refillMonitor) {
            while (isRefilling.get() && remainingNanos > 0) {
                try {
                    TimeUnit.NANOSECONDS.timedWait(refillMonitor, remainingNanos);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for hash cache refill", ex);
                }
                remainingNanos = deadline - System.nanoTime();
            }
        }

        if (isRefilling.get()) {
            log.warn("Timed out after {} ms waiting for hash cache refill", refillWaitTimeoutMs);
        }
    }

    private void completeRefill() {
        synchronized (refillMonitor) {
            isRefilling.set(false);
            refillMonitor.notifyAll();
        }
    }
}


