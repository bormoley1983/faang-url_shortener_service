package faang.school.urlshortenerservice.service;

import faang.school.urlshortenerservice.model.Hash;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Base62Encoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public List<Hash> encode(List<Long> numbers) {
        List<Hash> encoded = new ArrayList<>();

        for (Long number : numbers) {
            StringBuilder result = new StringBuilder();
            while (number > 0) {
                int index = (int) (number % BASE62_CHARS.length());
                result.append(BASE62_CHARS.charAt(index));
                number /= BASE62_CHARS.length();
            }
            encoded.add(new Hash(result.reverse().toString()));
        }

        return encoded;
    }
}
