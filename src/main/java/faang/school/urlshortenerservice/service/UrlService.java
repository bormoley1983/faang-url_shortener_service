package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Service;

@Service
public interface UrlService {

    public String createShortUrl(String longUrl);
}
