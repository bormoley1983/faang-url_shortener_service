package faang.school.urlshortenerservice.cache;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.hash.HashGenerator;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalCache {

    private final HashGenerator hashGenerator;

    @Value("${hash.cache.capacity:1000}")
    private int capacity;

    private final Queue<String> hashes = new ArrayBlockingQueue<>(capacity);

    @PostConstruct
    public void init() {
        List<Hash> hashesFromDb = hashGenerator.getHashes(capacity);
        hashes.addAll(hashesFromDb.toString());
    }

//    public String getHash() {
//        //TODO пропистаь проверку, чтобы если ниже какого-то порога, пора дополнить из бд
//        //учесть момент, что один уже пошел за кешом, другим идти не надо (разные потоки)
//        return hashes.poll();
//    }
}
