package faang.school.urlshortenerservice.service.hash;

import java.util.List;


public interface HashService {

    List<String> getHashes(long hashLimit);

    void generateHash();

    void saveHashByBatch(List<String> hashes);

}