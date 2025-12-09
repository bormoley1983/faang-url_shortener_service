package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class LocalHash {

    private final ConcurrentLinkedQueue<Hash> localHash = new ConcurrentLinkedQueue<>();

    private final HashGenerator hashGenerator;

    public Hash getLocalHash() {
        if(localHash.poll()==null){
            generateLocalHash();
        }
        return localHash.poll();
    }

    @Transactional
    private void generateLocalHash() {
        List<Hash> hash = hashGenerator.getHash();
        localHash.addAll(hash);
    }
}
