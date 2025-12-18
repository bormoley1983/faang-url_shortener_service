package faang.school.urlshortenerservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlBuilderServiceImpl implements UrlBuilderService {
    @Value("${url.host}")
    private String host;

    public String buildShortUrl(String hash) {
        return host + "/" + hash;
    }
}