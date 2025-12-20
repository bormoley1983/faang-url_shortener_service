package faang.school.urlshortenerservice.repository;

import java.util.Optional;

public interface UrlCacheRepository {

    public void save(String hash, String url);

    public Optional<String> getUrlByHash(String hash);
}
