package faang.school.url_shortener_service.hash;

import faang.school.url_shortener_service.entity.Hash;
import faang.school.url_shortener_service.repository.HashRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class HashGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = BASE62_CHARS.length();

    private final HashRepository hashRepository;

    public List<Hash> getHash() {
        return hashRepository.deleteAndReturnFirstN(100);
    }

    //toDO saveAll работает медленно, нужно сохранять бачами
    //подумать где распаралелить и как (узкие места)
    //сделать проверку количества в базе данных, если ниже то запускать HashGenerator
    public void generateHash() {
        List<Long> result = hashRepository.getNextRange(1000);
        List<Hash> resultListHash = generateHashByBase62(result);
        hashRepository.saveAll(resultListHash);

    }

    public List<Hash> generateHashByBase62(List<Long> listNumbers) {

        return listNumbers.parallelStream()
                .map(number -> new Hash(encodeBase62(number)))
                .toList();
    }

    private String encodeBase62(Long number) {

        StringBuilder result = new StringBuilder();
        long temp = number;

        while (temp > 0) {
            int remainder = (int) (temp % BASE);
            result.insert(0, BASE62_CHARS.charAt(remainder));
            temp = temp / BASE;
        }

        return result.toString();
    }
}