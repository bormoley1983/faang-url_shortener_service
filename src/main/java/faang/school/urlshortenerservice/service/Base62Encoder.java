package faang.school.urlshortenerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Base62Encoder {

    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_ALPHABET.length();

    /**
     * Кодирует список уникальных чисел в список base62 хэшей
     *
     * @param numbers список уникальных чисел для кодирования
     * @return список base62 хэшей
     */
    public List<String> encode(List<Long> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            log.debug("Empty or null input list provided");
            return new ArrayList<>();
        }

        List<String> encodedHashes = new ArrayList<>(numbers.size());

        for (Long number : numbers) {
            if (number == null) {
                log.warn("Null number found in input list, skipping");
                continue;
            }

            String hash = encodeNumber(number);
            encodedHashes.add(hash);
            log.trace("Encoded number {} to hash {}", number, hash);
        }

        log.debug("Successfully encoded {} numbers to base62 hashes", encodedHashes.size());
        return encodedHashes;
    }

    /**
     * Кодирует одно число в base62 строку
     *
     * @param number число для кодирования
     * @return base62 хэш
     */
    private String encodeNumber(Long number) {
        if (number == 0) {
            return String.valueOf(BASE62_ALPHABET.charAt(0));
        }

        StringBuilder encoded = new StringBuilder();
        long num = Math.abs(number);

        while (num > 0) {
            int remainder = (int) (num % BASE);
            encoded.append(BASE62_ALPHABET.charAt(remainder));
            num = num / BASE;
        }

        return encoded.reverse().toString();
    }
}