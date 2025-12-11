package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalHash {

    @Value("${hash.local.size.minimum:0.2}")
    private Double minimumSizeLocalHash;
    @Value("${hash.local.count.hash:1000}")
    private Integer countLocalHash;
    private final ConcurrentLinkedQueue<Hash> localHash = new ConcurrentLinkedQueue<>();

    private final HashGenerator hashGenerator;


    @PostConstruct
    public void initGenerateLocalHash() {
        generateLocalHash();
    }

    @Transactional
    public Hash getLocalHash() {
        int currentSizeLocalHash = localHash.size();
        double currentOccupancyPercentage = currentSizeLocalHash / (countLocalHash*1.0);

        log.info("current!!!!!!! {} {} {} ",currentOccupancyPercentage, currentSizeLocalHash, countLocalHash);
        if (currentOccupancyPercentage <= minimumSizeLocalHash) {
            // todo вызов асинхронно сделать
            generateLocalHash();
            log.info("the current local hash fullness is less {}%", currentOccupancyPercentage*100);
        }

        if (localHash.isEmpty()) {
            generateLocalHash();
            log.warn("local hash is empty!");
        }
        // todo есть возможность вернуть null
        return localHash.poll();
    }

    @Transactional
    public void generateLocalHash() {
        List<Hash> hash = hashGenerator.getHash();
        localHash.addAll(hash);
    }
}
