package faang.school.urlshortenerservice.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UrlRepository {
    public Optional<String> findLongUrlByHash(String hash) {
        return Optional.empty();
    }
}
