package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Base62Encoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public List<String> encode(List<Long> numbers) {
        List<String> encoded = new ArrayList<>();

        for (Long number : numbers) {
            if (number == null || number < 0) {
                throw new IllegalArgumentException("Number cannot be null");
            }
            if (number == 0) {
                encoded.add("0");
                continue;
            }

            StringBuilder result = new StringBuilder();
            while (number > 0) {
                int index = (int) (number % BASE62_CHARS.length());
                result.append(BASE62_CHARS.charAt(index));
                number /= BASE62_CHARS.length();
            }
            encoded.add(result.reverse().toString());
        }

        return encoded;
    }
}