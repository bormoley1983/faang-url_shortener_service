package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final HashCache hashCache;
    private final UrlRepository urlRepository;

    public String addLink(String url) {
        Url findUrl = urlRepository.findByUrl(url);
        if (findUrl == null) {
            Url newUrl = new Url(hashCache.get(), url, LocalDateTime.now());
            urlRepository.save(newUrl);
            return newUrl.getHash();
        }
        return findUrl.getHash();
    }

    public String findUrl(String hash) {
        Url findUrl = urlRepository.findByHash(hash);
        return findUrl.getUrl();
    }
}
