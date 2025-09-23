package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
@Slf4j
public class HashCache {

    private final HashGenerator hashGenerator;
    private CopyOnWriteArraySet<String> hashSet = new CopyOnWriteArraySet<>();
    private final HashRepository hashRepository;
    private final static int MAX_HASH_LENGTH = 32;

    @PostConstruct
    public void init() {
        List<Hash> content = hashRepository.findAll(PageRequest.of(0, MAX_HASH_LENGTH)).getContent();
        if (content.isEmpty() || content.size() < MAX_HASH_LENGTH) {
            hashGenerator.generateBatch();
        } else {
            content.forEach(hash -> hashSet.add(hash.getHash()));
        }
        System.out.println("hashSet = " + hashSet);
    }

    public void add(String value) {
        hashSet.add(value);
    }

    public String get() {
        Optional<String> any = hashSet.stream()
                .findAny();
        if (any.isPresent()) {
            hashSet.remove(any.get());
            if (hashSet.size() < MAX_HASH_LENGTH * 0.2) {
                log.info("1");
                hashSet.addAll(hashGenerator.generateBatch());
                log.info("Коллекция пополнена" + hashSet);
            }
            return any.get();
        } else {
            return null;
        }
    }
}
