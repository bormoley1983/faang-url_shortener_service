package faang.school.url_shortener_service.repository;

import faang.school.url_shortener_service.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {

}