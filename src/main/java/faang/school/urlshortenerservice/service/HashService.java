package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class HashService {

    private Integer numberOfHashes = 10;
    private final HashRepository hashRepository;

    public Long getUniqueNumber(long n) {
        return hashRepository.getUniqueNumber(n);
    }

    public void save(List<Hash> hashes) {
        hashRepository.saveAll(hashes);
    }

    public List<Hash> getHashBatch() {
        return hashRepository.findAll(PageRequest.of(0, numberOfHashes)).getContent();
    }
}
