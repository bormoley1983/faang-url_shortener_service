package faang.school.urlshortenerservice.util;

import faang.school.urlshortenerservice.entity.Hash;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Base62Encoder {

    private static final String DICTIONARY = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;

    public List<Hash> encode(List<Long> numbers) {
        return numbers.stream()
                .map(number -> {
                    StringBuilder hash = new StringBuilder();
                    while (number != 0) {
                        hash.append(DICTIONARY.charAt((int) (number % BASE)));
                        number /= BASE;
                    }
                    return Hash.builder()
                            .hash(hash.reverse().toString())
                            .build();
                })
                .toList();
    }
}
