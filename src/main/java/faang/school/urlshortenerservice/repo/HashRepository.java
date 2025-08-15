package faang.school.urlshortenerservice.repo;


import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, Long> {

    @Query(nativeQuery = true, value = "SELECT nextval('uniq_number_seq') FROM generate_series(1, :n)")
    List<Long> getUniqueNumbers(int n);
}