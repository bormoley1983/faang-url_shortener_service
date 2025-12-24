package faang.school.url_shortener_service.service;

import faang.school.url_shortener_service.entity.Hash;
import faang.school.url_shortener_service.entity.Url;
import faang.school.url_shortener_service.hash.LocalHash;
import faang.school.url_shortener_service.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortenerService {
    private final UrlRepository urlRepository;
    private final LocalHash localHash;

    //toDo прикрутить редис
    //в методе create нужно сохранять в Redis а в методе getUrl вначале получать из Редис, если нет получать из БД
    //и сохранить снова в Редис.
    public String getUrl(String hash) {
        Url result = urlRepository.getReferenceById(hash);
        return result.getLongLink();
    }

    public String create(String url) {
        Hash hash = localHash.getHash();

        Url urlRepo = new Url(hash.getHash(), url, LocalDateTime.now());
        Url result = urlRepository.save(urlRepo);
        log.info("URL has been saved to the repository");
        return result.getHash();
    }
}