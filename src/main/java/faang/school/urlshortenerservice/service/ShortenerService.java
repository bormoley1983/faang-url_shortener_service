package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.hash.HashGenerator;
import faang.school.urlshortenerservice.hash.LocalHash;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortenerService {
    private static final Integer POOL_SIZE_THREADS = 1500;

    private final ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE_THREADS);

    @Value(value = "${redis.hash.ttl}")
    private Integer daysTtl;
    @Value("${number.days.hash.storage}")
    private Integer daysStorageHashInBd;

    private final UrlRepository urlRepository;
    private final LocalHash localHash;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashGenerator hashGenerator;

    @Transactional
    public String create(String urlString) {

        Hash hash = localHash.getLocalHash();
        String hashString = hash.getHash();

        urlRepository.save(new Url(hashString, urlString, LocalDateTime.now()));
        CompletableFuture.runAsync(() -> redisTemplate.opsForValue().set(hashString, urlString, daysTtl, TimeUnit.MINUTES),
                executorService);
        log.info("get hash {} by url {}", hashString, urlString);
        return hash.getHash();
    }

    @Transactional
    public String getUrl(String hash) {
        String urlRedis;
        urlRedis = redisTemplate.opsForValue().get(hash);

        if (urlRedis == null) {
            Url url = urlRepository.findById(hash)
                    .orElseThrow(() -> new UrlNotFoundException("URL not found for hash by db: " + hash));
            checkAvailabilityInRedis(url, hash);
            log.info("get long URL {} by hash {}", url.getLongLing(), url.getHash());
            return url.getLongLing();
        }

        log.info("get URL {} from redis by hash {} ", urlRedis, hash);
        return urlRedis;
    }

    @Transactional
    public void cleanerUrlBd() {
        LocalDateTime dateBefore = LocalDateTime.now().minusDays(daysStorageHashInBd);
        List<Url> listDeletedUrl = urlRepository.deleteOlderThanAndReturn(dateBefore);

        List<Hash> hashReturnInPool = listDeletedUrl.stream()
                .map(url -> new Hash(url.getHash()))
                .toList();
        hashGenerator.saveHashesInBatches(hashReturnInPool);
    }


    private void checkAvailabilityInRedis(Url url, String hash) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cteatedAt = url.getCreatedAt();
        long daysDifference = daysTtl - Duration.between(now, cteatedAt).toDays();
        if (daysDifference > 0) {
            redisTemplate.opsForValue().set(hash, url.getLongLing(), daysDifference, TimeUnit.MINUTES);
        }
    }
}


