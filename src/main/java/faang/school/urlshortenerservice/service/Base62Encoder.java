package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Base62Encoder {

    private static final char[] BASE62_CHARS_ARRAY =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public String encode(Long number) {
        if (number == null || number < 0) {
            throw new IllegalArgumentException("Number cannot be null");
        }
        if (number == 0) {
            return String.valueOf(BASE62_CHARS_ARRAY[0]);
        }

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % BASE62_CHARS_ARRAY.length);
            result.append(BASE62_CHARS_ARRAY[remainder]);
            number /= BASE62_CHARS_ARRAY.length;
        }

        return result.reverse().toString();
    }

    public List<String> encodeTheList(List<Long> numbers) {
        List<String> encoded = new ArrayList<>();

        for (Long number : numbers) {
            encoded.add(encode(number));
        }
        return encoded;
    }
}