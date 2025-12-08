package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Url;
import faang.school.urlshortenerservice.hash.LocalHash;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShortenerService {

    private final UrlRepository urlRepository;
    private final LocalHash localHash;

    public String create(String urlString) {
        String hash = localHash.getLocalHash();
        Url url = Url.builder()
                .hash(hash)
                .longLing(urlString)
                .build();

        urlRepository.save(url);
        return url.getHash();
    }

    public String getUrl(String hash) {
        Url url = urlRepository.getReferenceById(hash);
        log.info("get long url {} by hash {}", url.getLongLing(), url.getHash());
        return url.getLongLing();
    }
}
