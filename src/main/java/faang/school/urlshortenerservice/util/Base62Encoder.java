package faang.school.urlshortenerservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Утилитарный компонент для кодирования чисел в Base62.
 *
 * <p>Преобразует числа в строки с использованием символов [0-9a-zA-Z],
 * применимо для генерации коротких хэшей URL.</p>
 */
@Component
@RequiredArgsConstructor
public class Base62Encoder {

    private static final char[] BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BASE = 62;

    /**
     * Преобразует список чисел в список Base62-хэшей.
     */
    public List<String> encode(List<Long> numbers) {
        List<String> hashes = new ArrayList<>(numbers.size());
        for (Long number : numbers) {
            hashes.add(toBase62(number));
        }
        return hashes;
    }

    private String toBase62(long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE62[(int) (number % BASE)]);
            number /= BASE;
        }
        String hash = sb.reverse().toString();
        if (hash.length() > 6) {
            hash = hash.substring(0, 6);
        }
        return hash;
    }
}