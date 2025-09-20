package faang.school.urlshortenerservice.encoder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Base62Encoder — кодирует числа в Base62.
 * <p>
 * Для для генерации коротких хэшей URL
 * </p>*
 *
 * @author andreyfomchenko
 * @since 17.09.2025
 */
@Component
@RequiredArgsConstructor
public class Base62Encoder {

    private static final char[] BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int BASE = 62;

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
