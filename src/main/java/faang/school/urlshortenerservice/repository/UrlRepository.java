package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<Url, String> {

    Boolean existsUrlByUrl(String url);
    Url findByUrl(String url);
    Url findByHash(String hash);
}
