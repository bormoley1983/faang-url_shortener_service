package faang.school.urlshortenerservice.job;

import faang.school.urlshortenerservice.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class JobCleanerUrlDb {

    @Value("${number.days.hash.storage}")
    private Integer daysStorageHashInBd;

    private final UrlRepository urlRepository;

    @Transactional
    public void cleanerUrlDb() {
        LocalDateTime dateBefore = LocalDateTime.now().minusDays(daysStorageHashInBd);
           urlRepository.deleteOlderThan(dateBefore);
    }

}
