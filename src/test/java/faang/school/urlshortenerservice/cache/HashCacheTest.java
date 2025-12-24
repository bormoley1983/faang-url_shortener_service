package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.repository.UniqueNumbersRepository;
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
    private UniqueNumbersRepository repository;
    @Mock
    private HashGenerator hashGenerator;
    @InjectMocks
    private HashCache hashCache;

    @Test
    @DisplayName("Тестирование получения хэша")
    void getHash() {
        hashCache.generateAndGetHashesBatch();
        verify(hashGenerator).generateHashes();
    }
}
