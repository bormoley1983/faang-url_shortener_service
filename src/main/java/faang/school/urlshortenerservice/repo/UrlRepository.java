package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.Url;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByHash(@NotBlank String shortUrl);
}
