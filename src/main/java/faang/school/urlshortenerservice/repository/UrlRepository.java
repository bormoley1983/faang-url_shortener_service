package faang.school.urlshortenerservice.repository;

import faang.school.urlshortenerservice.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByHash(String hash);

    List<Url> findAllByExpiresAtBefore(Instant cutoff);

    @Modifying
    @Transactional
    @Query("DELETE FROM Url u WHERE u.hash IN :hashes")
    void deleteByHashIn(List<String> hashes);
}
