package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.short_url.CreateShortUrlDto;

public interface UrlService {
    String createShortUrl(CreateShortUrlDto dto);

    String getOriginalUrl(String hash);
}
