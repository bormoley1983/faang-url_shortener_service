package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.repository.HashRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HashCacheTest {
    @Mock
    private HashRepository repository;
    @Mock
    private HashGenerator hashGenerator;
    @InjectMocks
    private HashCache hashCache;
    @Value("${spring.jpa.hibernate.batch_size}")
    private Long batchSize;

    @Test
    @DisplayName("Тестирование получения хэша")
    void getHash() {
        hashCache.generateAndGetHashesBatch();
        verify(repository).getHashesBatch(batchSize);
        verify(repository).deleteAllByIdInBatch(any());
        verify(hashGenerator).generateHashes();
    }
}
