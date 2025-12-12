package faang.school.urlshortenerservice.encoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Base62Encoder {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    /**
     * Кодирует список чисел в Base62 хэши
     *
     * @param numbers список уникальных чисел
     * @return список Base62 хэшей
     */
    public List<String> encode(List<Long> numbers) {
        log.debug("Encoding {} numbers to Base62", numbers.size());

        List<String> hashes = numbers.stream()
                .map(this::encodeNumber)
                .collect(Collectors.toList());

        log.debug("Successfully encoded {} hashes", hashes.size());
        return hashes;
    }

    /**
     * Кодирует одно число в Base62
     *
     * @param number число для кодирования
     * @return Base62 хэш
     */
    private String encodeNumber(long number) {
        if (number == 0) {
            return String.valueOf(BASE62_ALPHABET.charAt(0));
        }

        StringBuilder hash = new StringBuilder();
        long num = number;

        while (num > 0) {
            int remainder = (int) (num % BASE);
            hash.insert(0, BASE62_ALPHABET.charAt(remainder));
            num = num / BASE;
        }

        return hash.toString();
    }
}