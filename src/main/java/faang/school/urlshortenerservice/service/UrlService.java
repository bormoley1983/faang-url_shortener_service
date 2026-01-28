package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Service;

@Service
public interface UrlService {

    String createShortUrl(String longUrl);

    String getOriginalUrl(String hash);
}
