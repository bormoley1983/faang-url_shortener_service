package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Analytic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticRepository extends JpaRepository<Analytic, Long> {
}
