package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HashRepository extends JpaRepository<Hash, String> {

    @Modifying
    @Query(nativeQuery = true, value = """
                SELECT nextval('unique_number_seq') FROM generate_series(1, :count)
            """)
    List<Long> getUniqueNumbers(@Param("count") Long count);
}
