package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, String> {

    @Query("""
            select u.url from Url u
                    where u.hash = :hash
            """)
    Optional<String> findUrlByHash(@Param("hash") String hash);

    @Modifying
    @Query(value = """
            DELETE FROM url_hash 
            WHERE create_at < CURRENT_DATE - INTERVAL '1 year' 
            RETURNING hash
            """, nativeQuery = true)
    List<String> cleanUnusedHash();
}