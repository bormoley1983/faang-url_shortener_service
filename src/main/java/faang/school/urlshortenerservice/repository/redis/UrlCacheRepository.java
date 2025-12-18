package faang.school.urlshortenerservice.repository.redis;

public interface UrlCacheRepository {
    void save(String hash, String longUrl);
}
