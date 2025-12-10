package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.generator.HashGenerator;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.LocalCache;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UrlServiceImpl implements UrlService {
    private final LocalCache localCache;
    private final UrlRepository urlRepository;
    private final HashGenerator hashGenerator;
    private final UrlCacheRepository urlCacheRepository;
    private final HashRepository hashRepository;
    @Value("${domain.prefix}")
    private String domain;

    @Override
    @Transactional
    public String createUrls(String userUrl) {
        log.info("Request to create a short link");
        StringBuilder responseUrl = new StringBuilder();
        String hash = localCache.getHash();
        Url url = Url.builder()
                .hash(hash)
                .url(userUrl)
                .build();
        urlRepository.save(url);
        urlCacheRepository.cacheUrl(hash, userUrl);
        responseUrl.append(domain);
        responseUrl.append(hash);
        log.info("Successful create short link. Origin link {}, short link {}", userUrl, responseUrl);
        return responseUrl.toString();
    }

    @Override
    public String getUrl(String hash) {
        log.info("Request to redirect to original link {}", hash);
        if (hash.isBlank()) {
            throw new UrlNotFoundException("Unsupported link view");
        }
        Optional<String> url = urlCacheRepository.getCachedUrl(hash);
        if (url.isEmpty()) {
            log.info("Radish does not have the original reference {}. Checking in the database", url);
            url = urlRepository.findUrlByHash(hash);
        }
        return url.orElseThrow(() -> new UrlNotFoundException("Url not found"));
    }

    @Override
    @Transactional
    public void cleanHash() {
        long startTime = System.currentTimeMillis();
        List<String> unusedHash = urlRepository.cleanUnusedHash();
        hashGenerator.saveHashByBatch(unusedHash);
        long finishTime = System.currentTimeMillis() - startTime;
        log.info("Successful removal of unused {} hashes in {} ms", unusedHash.size(), finishTime);
    }

    @Override
    @Transactional(readOnly = true)
    public long countHashRepository() {
        return hashRepository.count();
    }
}