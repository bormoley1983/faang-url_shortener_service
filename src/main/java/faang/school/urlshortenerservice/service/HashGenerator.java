package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashGenerator {

    private final HashService hashService;
    private final HashRepository hashRepository;

    public void generateBatch() {
        List<Long> uniqueNumbers = hashService.getUniqueNumbers(20);
        Base64.Encoder encoder = Base64.getEncoder();

        List<Hash> hashes = uniqueNumbers.stream()
                .map(uniqueNumber -> {
                    byte[] array = new byte[1];
                    array[0] = uniqueNumber.byteValue();
                    String hash = encoder.encodeToString(array);
                    Hash newHash = new Hash(hash);
                    return newHash;
                }).toList();

        hashService.save(hashes);
    }
}
