package faang.school.urlshortenerservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.entity.UrlRedis;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.hash.LocalHash;
import faang.school.urlshortenerservice.job.JobCleanerUrlDb;
import faang.school.urlshortenerservice.repository.UrlRedisRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortenerService {

    private final UrlRepository urlRepository;
    private final LocalHash localHash;
    private final JobCleanerUrlDb jobCleanerUrlDb;
    private final UrlRedisRepository urlRedisRepository;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> redisTemplateObject;


    // todo добавить сохранение в редис
    @Transactional
    public String create(String urlString) {

        Long DbTime = System.nanoTime();
        Hash hash = localHash.getLocalHash();
        Url url = Url.builder()
                .hash(hash.getHash())
                .longLing(urlString)
                .build();
        Url result = urlRepository.save(url);
        log.info("time from DB {}", System.nanoTime() - DbTime);
        //через DB
        //37280800
        //31579100

     //    тут сохронял через @RedisHash, но по времени показал самые худшие результаты
     // даже медленнее чем сохрание в бд
        //  Long redisTime1 = System.nanoTime();
        //  UrlRedis urlRedis = UrlRedis.builder()
        //          .hash(hash.getHash())
        //          .longLing(urlString)
        //          .build();
        //  urlRedisRepository.save(urlRedis);
        //  log.info("time from redis {}",   System.nanoTime() - redisTime1);
       // через urlRedisRepository @RedisHash
       //время 379463800
       //время 333947400

        Long redisTime2= System.nanoTime();
        redisTemplate.opsForValue().set(hash.getHash(), url.getLongLing(), 30, TimeUnit.DAYS);
        log.info("--- time from redis {}",   System.nanoTime() - redisTime2);
        //через redis template
        //время 4013500  значение строка
        //время 13006800 значение объект

        log.info("get hash {} by url {}", url.getHash(), url.getLongLing());
        return result.getHash();
    }

    // todo вначале проверять из бд
    @Transactional
    public String getUrl(String hash) {

        Long redisTime1 = System.nanoTime();
        String key = "urls:" + hash;
        urlRedisRepository.findById(hash)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for hash by redis: " + hash));;
        log.info("{} time from redis {}", key,  System.nanoTime() - redisTime1);


        Long redisTime2= System.nanoTime();
        redisTemplate.opsForHash().entries(key);
        log.info("{} time from redis {}", key,  System.nanoTime() - redisTime2);

        Long DbTime = System.nanoTime();
        Url url = urlRepository.findById(hash)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for hash by db: " + hash));
        log.info("get long url {} by hash {}", url.getLongLing(), url.getHash());
        log.info("time from DB {}", System.nanoTime() - DbTime);

        return url.getLongLing();
    }

    @Scheduled(cron = "${app.sheduled.time.cleaner}")
    public void cleanerUrlBd() {
        jobCleanerUrlDb.cleanerUrlDb();
    }

}
