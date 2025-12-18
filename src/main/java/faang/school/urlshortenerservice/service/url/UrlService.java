package faang.school.urlshortenerservice.service.url;

import faang.school.urlshortenerservice.dto.UrlDto;

public interface UrlService {

    UrlDto createUrl(String userUrl);

    String getUrl(String requestUrl);

    void cleanHash();

    long countHashRepository();
}