package faang.school.urlshortenerservice.base62encoder;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Base62Encoder {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Преобразует список чисел в список Base62-хэшей
     * @param numbers список уникальных чисел
     * @return список хэшей
     */
    public List<String> encode(List<Long> numbers) {
        List<String> hashes = new ArrayList<>(numbers.size());
        for (Long num : numbers) {
            hashes.add(encodeNumber(num));
        }
        return hashes;
    }

    private String encodeNumber(long num) {
        if (num == 0) return "0";

        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int rem = (int) (num % 62);
            sb.append(BASE62.charAt(rem));
            num /= 62;
        }
        return sb.reverse().toString();
    }
}
