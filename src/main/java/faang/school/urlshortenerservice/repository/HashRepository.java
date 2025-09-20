package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.HashEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HashRepository extends JpaRepository<HashEntity, String>, HashRepositoryUtil {
}
