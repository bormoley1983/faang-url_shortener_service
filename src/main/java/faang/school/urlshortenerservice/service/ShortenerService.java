package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.hash.LocalHash;
import faang.school.urlshortenerservice.job.JobService;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortenerService {

    @Value(value = "${redis.hash.ttl}")
    private Integer daysTtl;

    @Value("${number.days.hash.storage}")
    private Integer daysStorageHashInBd;

    private final UrlRepository urlRepository;
    private final LocalHash localHash;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public String create(String urlString) {

        Hash hash = localHash.getLocalHash();
        Url url = Url.builder()
                .hash(hash.getHash())
                .longLing(urlString)
                .build();
        Url result = urlRepository.save(url);

        redisTemplate.opsForValue().set(hash.getHash(), url.getLongLing(), daysTtl, TimeUnit.MINUTES);

        log.info("get hash {} by url {}", url.getHash(), url.getLongLing());
        return result.getHash();
    }

    @Transactional
    public String getUrl(String hash) {
        String urlRedis;
        urlRedis =  redisTemplate.opsForValue().get(hash);

        if (urlRedis == null) {
            Url url = urlRepository.findById(hash)
                    .orElseThrow(() -> new UrlNotFoundException("URL not found for hash by db: " + hash));
            log.info("get long URL {} by hash {}", url.getLongLing(), url.getHash());
            return url.getLongLing();
        }

        log.info("get URL {} from redis by hash {} ", urlRedis, hash);
        return urlRedis;
    }

    @Transactional
    public void cleanerUrlBd() {
        LocalDateTime dateBefore = LocalDateTime.now().minusDays(daysStorageHashInBd);
        urlRepository.deleteOlderThan(dateBefore);
        // todo сюда добавить возвращение хешей
    }
}
