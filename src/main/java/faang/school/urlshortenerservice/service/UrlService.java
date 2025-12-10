package faang.school.urlshortenerservice.service;

public interface UrlService {

    String createUrls(String userUrl);

    String getUrl(String requestUrl);

    void cleanHash();

    long countHashRepository();
}