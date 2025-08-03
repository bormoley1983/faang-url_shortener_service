package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface HashesRepository extends JpaRepository<Hash, String> {

    @Query(value = """
            SELECT nextval('unique_number_seq') FROM generate_series(1, :qty)
            """, nativeQuery = true)
    List<BigInteger> getUniqueNumbers(@Param("qty") Long qty);

    @Modifying
    @Query(value = """
            INSERT INTO hashes(hash) SELECT unnest FROM unnest(:hashes)
            """, nativeQuery = true)
    void save(@Param("hashes") String[] hashes);

    @Query(value = """
            DELETE FROM hashes WHERE hash IN (SELECT hash FROM hashes order by random() LIMIT :qty) returning hash
            """, nativeQuery = true)
    List<String> getHashBatch(@Param("qty") Long qty);
}
