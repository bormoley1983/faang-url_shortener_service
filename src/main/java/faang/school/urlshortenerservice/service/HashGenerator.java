package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import faang.school.urlshortenerservice.repository.UniqueIdRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HashGenerator {
    private final UniqueIdRepository uniqueIdRepository;
    private final HashRepository hashRepository;
    private final Base62Encoder base62Encoder;
    private final EntityManager em;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private Integer chunkSize;

    @Transactional
    public void generateAndSaveHashes(int count) {
        long startTime = System.currentTimeMillis();

        List<Long> ids = uniqueIdRepository.getNextRange(count);

        List<Hash> buffer = new ArrayList<>(chunkSize);

        for (Long id : ids) {
            buffer.add(Hash.builder()
                    .hashValue(base62Encoder.encode(id))
                    .isUsed(false)
                    .build());

            if (buffer.size() == chunkSize) {
                hashRepository.saveAll(buffer);
                hashRepository.flush();
                em.clear();
                buffer.clear();
            }
        }

        if (!buffer.isEmpty()) {
            hashRepository.saveAll(buffer);
            hashRepository.flush();
            em.clear();
        }

        log.warn("Generated and saved {} hashes in {} ms", count, System.currentTimeMillis() - startTime);
    }
}
