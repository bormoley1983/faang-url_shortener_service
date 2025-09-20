package faang.school.urlshortenerservice.repository;


public interface UrlCacheRepository {

    void save(String url, String hash);

    String get(String hash);
}
