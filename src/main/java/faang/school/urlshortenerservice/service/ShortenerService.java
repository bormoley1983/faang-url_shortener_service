package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.hash.LocalHash;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortenerService {

    private final UrlRepository urlRepository;
    private final LocalHash localHash;

    // todo добавить сохранение в редис
    @Transactional
    public String create(String urlString) {
        Hash hash = localHash.getLocalHash();
        Url url = Url.builder()
                .hash(hash.getHash())
                .longLing(urlString)
                .build();

        Url result = urlRepository.save(url);
        log.info("get hash {} by url {}", url.getHash(), url.getLongLing());
        return result.getHash();
    }

    // todo вначале проверять из бд
    @Transactional
    public String getUrl(String hash) {
        Url url = urlRepository.findById(hash)
                .orElseThrow(() -> new UrlNotFoundException("URL not found for hash: " + hash));
        log.info("get long url {} by hash {}", url.getLongLing(), url.getHash());
        return url.getLongLing();
    }

    //todo добавить метод, который бы очищал таблицу с не используемыми хешами
}
