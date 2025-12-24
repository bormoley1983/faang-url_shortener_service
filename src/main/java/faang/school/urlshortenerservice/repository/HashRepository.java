package faang.school.urlshortenerservice.repository;

import java.util.List;

public interface HashRepository {

    List<Long> getUniqueNumbers(Integer count);

    void save(List<String> hashes);

    List<String> getHashBatch();
}
