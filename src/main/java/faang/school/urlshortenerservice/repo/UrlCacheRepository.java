package faang.school.urlshortenerservice.repo;

import java.util.Optional;

public interface UrlCacheRepository {

    void save(String hash, String longUrl);

    Optional<String> findLongUrl(String hash);
}
