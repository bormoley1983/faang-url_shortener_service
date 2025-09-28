package faang.school.urlshortenerservice.encoder;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Base62Encoder {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public List<Hash> encode(List<Long> numbers) {
        return numbers.stream().map(number -> {
            StringBuilder sb = new StringBuilder();
            while (number > 0) {
                sb.append(BASE62_CHARS.charAt((int) (number % BASE62_CHARS.length())));
                number /= BASE62_CHARS.length();
            }
            return Hash.builder().hashValue(sb.reverse().toString()).build();
        }).toList();
    }
}
