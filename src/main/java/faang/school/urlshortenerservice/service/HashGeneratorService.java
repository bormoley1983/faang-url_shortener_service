package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.encoder.Base62Encoder;
import faang.school.urlshortenerservice.repository.HashRepositoryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HashGeneratorService {

    private final HashRepositoryUtil hashRepository;
    private final Base62Encoder base62;

    @Async("hashGeneratorExecutor")
    public void generateHashes(int batchSize) {
        List<Long> uniqueNumber = hashRepository.getUniqueNumbers(batchSize);
        List<String> generatedHashes = base62.encode(uniqueNumber);
        hashRepository.save(generatedHashes);
    }
}
