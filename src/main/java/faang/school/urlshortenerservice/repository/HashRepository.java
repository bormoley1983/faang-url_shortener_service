package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Репозиторий для работы с таблицей hash и unique_number_seq. Имеет методы для получения
 * батча уникальных возрастающих номеров и получения батча уникальных хэшей
 */
public interface HashRepository extends JpaRepository<Hash, String> {

    @Query(nativeQuery = true, value = """
                SELECT nextval('unique_number_seq') FROM generate_series(1, :count)
            """)
    List<Long> getUniqueNumbers(@Param("count") Long count);

    @Query(nativeQuery = true, value = """
                SELECT hash FROM hash LIMIT :limit
            """)
    List<String> getHashesBatch(@Param("limit") Long limit);

}
