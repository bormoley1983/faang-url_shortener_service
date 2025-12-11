package faang.school.urlshortenerservice.generator;

import faang.school.urlshortenerservice.repository.HashRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private static final String BASE_62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private final HashRepository hashRepository;

    @Value("${hash.range:10000}")
    private int maxRange;

    @Transactional
    public void generateHash(){
        List<Long> range = hashRepository.getNextRange(maxRange);
        range.forEach(number -> {
            String hash = applyBase62Encoding(number);
            hashRepository.saveHash(number, hash);
        });
    }

    private String applyBase62Encoding(long number) {
        StringBuilder builder = new StringBuilder();
        while (number > 0) {
            builder.append(BASE_62.charAt((int) (number % BASE_62.length())));
            number /= BASE_62.length();
        }
        return builder.toString();
    }
}
