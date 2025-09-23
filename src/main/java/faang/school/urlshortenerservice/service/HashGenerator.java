package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashGenerator {

    private final HashService hashService;

    @Async("taskExecutor")
    public List<String> generateBatch() {
       List<Long> uniqueNumbers = new ArrayList<>();
        Base64.Encoder encoder = Base64.getEncoder();

        for (int i = 0; i < 20; i++) {
            uniqueNumbers.add(hashService.getUniqueNumber(1));
        }

        List<Hash> hashes = uniqueNumbers.stream()
                .map(uniqueNumber -> {
                    byte[] array = new byte[1];
                    array[0] = uniqueNumber.byteValue();
                    String hash = encoder.encodeToString(array);
                    Hash newHash = new Hash(hash);
                    return newHash;
                }).toList();

        hashService.save(hashes);
        return hashes.stream().map(Hash::getHash).collect(Collectors.toList());
    }
}
