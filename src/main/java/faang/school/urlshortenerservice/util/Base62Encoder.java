package faang.school.urlshortenerservice.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс, реализующий логику кодирования в base62
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Component
public class Base62Encoder {

    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final char[] BASE62_CHARS = BASE62_ALPHABET.toCharArray();
    private static final int POWER_ALPHABET = BASE62_ALPHABET.length();

    public List<String> encode(List<Long> numbers) {
        List<String> result = new ArrayList<>();

        for (Long num : numbers) {
            result.add(encode(num));
        }

        return result;
    }

    public String encode(Long number) {
        if (isInvalidNumber(number)) {
            throw new IllegalArgumentException("Передано невалидное число для кодирования: " + number);
        }

        if (number == 0) {
            return String.valueOf(BASE62_CHARS[0]);
        }

        StringBuilder result = new StringBuilder();
        while (number != 0) {
            int index = (int) (number % POWER_ALPHABET);
            result.append(BASE62_CHARS[index]);
            number /= POWER_ALPHABET;
        }

        return result.reverse().toString();
    }

    private boolean isInvalidNumber(Long number) {
        return number == null || number < 0;
    }
}