package faang.school.urlshortenerservice.repo;

import faang.school.urlshortenerservice.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    @Modifying
    @Query(nativeQuery = true, value = """
        DELETE FROM url
        WHERE created_at < NOW() - INTERVAL '1 year'
        RETURNING hash
        """)
    List<String> deleteOldUrlsAndReturnHashes();

}