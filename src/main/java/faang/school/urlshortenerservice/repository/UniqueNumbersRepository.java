package faang.school.urlshortenerservice.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Репозиторий для работы с unique_number_seq. Имеет метод для получения батча уникальных
 * возрастающих номеров
 */
@Repository
@RequiredArgsConstructor
public class UniqueNumbersRepository {

    private final EntityManager em;

    @Transactional
    public List<Long> getUniqueNumbers(Long count) {
        String sql = "SELECT nextval('unique_number_seq') FROM generate_series(1, ?1)";
        return em.createNativeQuery(sql)
                .setParameter(1, count)
                .getResultList();
    }
}
