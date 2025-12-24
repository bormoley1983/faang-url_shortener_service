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
public class HashGeneratorTest {
    @Mock
    private UniqueNumbersRepository repository;
    @Mock
    private Base62Encoder encoder;
    @InjectMocks
    private HashGenerator hashGenerator;
    @Value("${spring.jpa.hibernate.batch_size}")
    private Long batchSize;

    @Test
    @DisplayName("Тестирование генерации хэшей")
    void generateHashesTest() {
        hashGenerator.generateHashes();
        verify(repository).getUniqueNumbers(batchSize);
        verify(encoder).encodeNumbers(any());
    }
}
