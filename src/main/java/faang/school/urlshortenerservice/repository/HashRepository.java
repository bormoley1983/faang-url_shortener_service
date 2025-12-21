package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HashRepository extends JpaRepository<Hash, Long> {
    @Query(value = """
            SELECT * FROM hashes
            WHERE is_used = false
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Hash> findUnusedHashes(int limit);

    @Query(value = """
            SELECT count(h) FROM Hash h
            WHERE h.isUsed = false
            """)
    Long countUnusedHashes();

    @Modifying
    @Query("""
            UPDATE Hash h
            SET h.isUsed = true
            WHERE h.hashValue IN :hashValues
            """)
    void markAsUsed(List<String> hashValues);
}
