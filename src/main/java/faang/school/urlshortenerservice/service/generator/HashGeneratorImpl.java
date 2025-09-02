package faang.school.urlshortenerservice.service.generator;

import faang.school.urlshortenerservice.common.encoder.Encoder;
import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class HashGeneratorImpl implements HashGenerator {
    @Value("${hash.generator.quantity}")
    private int quantity;

    private final HashRepository repository;
    private final Encoder encoder;

    @Override
    public void generateBatch() {
        List<Long> uniqueNumbers = repository.getUniqueNumbers(quantity);
        List<Hash> hashes = encoder
                .encode(uniqueNumbers).stream()
                .map(Hash::new)
                .toList();
        repository.saveAll(hashes);
    }

    @Override
    @Async("hashGeneratorExecutorService")
    public void generateBatchAsync() {
        generateBatch();
    }
}
