package faang.school.urlshortenerservice.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlRepository {

    List<String> deleteOldUrlsAndReturnHashes(LocalDateTime olderThan);

    Optional<String> findUrlByHash(String hash);

    void save(String hash, String url);
}
