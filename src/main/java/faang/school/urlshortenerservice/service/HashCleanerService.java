package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashCleanerService {
    // idempotent re-insert so concurrent replicas (or a hash that was already
    // recycled by another replica) cannot fail the cleanup with a unique-constraint error.
    private static final String RECYCLE_HASH_SQL = """
            INSERT INTO hash (hash) VALUES (?) ON CONFLICT (hash) DO NOTHING
            """;

    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void cleanupOutdatedHashes() {
        List<String> retrievedHashes = urlRepository.deleteExpiredUrlsAndReturnHashes();
        if (retrievedHashes.isEmpty()) {
            log.info("no outdated short links found");
            return;
        }

        log.info("{} of outdated short links found and removed.", retrievedHashes.size());

        for (String hash : retrievedHashes) {
            jdbcTemplate.update(RECYCLE_HASH_SQL, hash);
            // evict the stale cache entry so a recycled code cannot redirect to
            // the prior destination from any replica's shared Redis.
            urlCacheRepository.deleteByHash(hash);
        }
    }
}
