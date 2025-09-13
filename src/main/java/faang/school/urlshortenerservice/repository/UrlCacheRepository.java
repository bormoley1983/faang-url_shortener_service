package faang.school.urlshortenerservice.repository;

public interface UrlCacheRepository {

    String findUrlByHash(String hash);

    void saveUrl(String hash, String originalUrl);
}
