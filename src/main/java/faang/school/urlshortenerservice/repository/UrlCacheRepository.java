package faang.school.urlshortenerservice.repository;

import java.util.Optional;

public interface UrlCacheRepository {

    void save(String hash, String url);

    Optional<String> getUrlByHash(String hash);
}
