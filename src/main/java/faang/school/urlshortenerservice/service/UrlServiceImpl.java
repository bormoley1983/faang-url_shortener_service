package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.LocalCache;
import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UrlServiceImpl implements UrlService {
    private final LocalCache localCache;
    private final UrlRepository urlRepository;
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
        responseUrl.append(domain);
        responseUrl.append(localCache.getHash());
        log.info("Successful create short link. Origin link {}, short link {}", userUrl, responseUrl);
        return responseUrl.toString();
    }

    @Override
    public String getUrl(String requestUrl) {
        log.info("Request to redirect to original link");
        String hash = requestUrl.substring(requestUrl.lastIndexOf("/") + 1);
        // Todo не работает возврат ссылки, редирект api/v1/url-shortener
        String url = urlRepository.findUrlByHash(hash);
        return url;
    }
}