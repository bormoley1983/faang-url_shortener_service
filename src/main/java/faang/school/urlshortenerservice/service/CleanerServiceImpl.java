package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.config.cleaner.CleanerProperties;
import faang.school.urlshortenerservice.repository.db.HashRepository;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanerServiceImpl implements CleanerService {
    private final UrlRepository urlRepository;
    private final HashRepository hashRepository;
    private final CleanerProperties props;

    @Transactional
    @Override
    public int clean() {
        OffsetDateTime threshold = OffsetDateTime.now().minus(props.getRetention());

        List<String> freedHashes = urlRepository.deleteExpiredReturningHashes(threshold);
        if (freedHashes.isEmpty()) {
            log.info("Cleaner: nothing to delete. threshold={}", threshold);
            return 0;
        }

        hashRepository.save(freedHashes);

        log.info("Cleaner: deleted {} url rows and returned {} hashes. threshold={}",
                freedHashes.size(), freedHashes.size(), threshold);

        return freedHashes.size();
    }
}
