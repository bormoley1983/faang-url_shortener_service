package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HashRepository extends JpaRepository<Hash, String> {

    @Query(value = "select nextVal('unique_number_seq') from generated_series(1, :size)", nativeQuery = true)
    List<Long> getUniqueNumbers(@Param("size") Long size);

}
