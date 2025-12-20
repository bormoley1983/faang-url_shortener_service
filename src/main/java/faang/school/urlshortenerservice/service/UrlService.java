package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.dto.UrlDto;

public interface UrlService {

    void cleanOldUrls();

    UrlDto getUrl(String hash);

    String createShortUrl(UrlDto urlDto);
}
