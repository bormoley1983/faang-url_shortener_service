package faang.school.urlshortenerservice.repository.db;

import faang.school.urlshortenerservice.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlEntity, String> {
}
