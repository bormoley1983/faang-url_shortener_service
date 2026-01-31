package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.model.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashCleanerService {
    private final HashRepository hashRepository;
    private final UrlRepository urlRepository;
    private final UrlCacheRepository urlCacheRepository;

    @Transactional
    public void cleanupOutdatedHashes() {
        List<String> retrievedHashes = urlRepository.deleteExpiredUrlsAndReturnHashes();
        if (retrievedHashes.isEmpty()) {
            log.info("no outdated short links found");
            return;
        }

        log.info("{} of outdated short links found and removed.", retrievedHashes.size());

        List<Hash> hashEntities = retrievedHashes.stream()
            .map(Hash::new)
            .collect(Collectors.toList());

        hashRepository.saveAll(hashEntities);
        retrievedHashes.forEach(urlCacheRepository::deleteByHash);
    }
}
