package faang.school.urlshortenerservice.hash;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class LocalHash {

    private final ConcurrentLinkedQueue<String> localHash = new ConcurrentLinkedQueue<>();

    private final HashGenerator hashGenerator;

    public String getLocalHash() {
        if(localHash.poll()==null){
            generateLocalHash();
        }
        return localHash.poll();
    }

    private void generateLocalHash() {
        List<String> hash = hashGenerator.getHash();
        localHash.addAll(hash);
    }
}
