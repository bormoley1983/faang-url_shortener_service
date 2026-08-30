package faang.school.urlshortenerservice.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Base62Encoder {
    private static final String BASE62_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int BASE = BASE62_CHARACTERS.length();
    private static final int LEN = 6;

    public List<String> encode(List<Long> randomNumbersList) {
        UniqueValuesListValidator.validateList(randomNumbersList, "Supplied list of numbers is empty!");
        UniqueValuesListValidator.validateUniqueness(randomNumbersList);

        return randomNumbersList.stream()
                .map(num -> encode(num))
                .collect(Collectors.toList());
    }

        public List<String> encodeFixed(List<Long> randomNumbersList) {
        UniqueValuesListValidator.validateList(randomNumbersList, "Supplied list of numbers is empty!");
        UniqueValuesListValidator.validateUniqueness(randomNumbersList);

        return randomNumbersList.stream()
                .map(num -> encodeFixed(num, LEN))
                .collect(Collectors.toList());
    }
    
    public String encodeFixed(long number) {
        return encodeFixed(number, LEN);
    }

    private String encode(long number) {
        int capacity = Math.max(1, (int) (Math.log(number + 1) / Math.log(BASE)) + 1);
        StringBuilder stringBuilder = new StringBuilder(capacity);

        do {
            stringBuilder.insert(0, BASE62_CHARACTERS.charAt((int) (number % BASE)));
            number /= BASE;
        } while (number > 0);

        return stringBuilder.toString();
    }

    private String encodeFixed(long number, int length) {
        if (length <= 0) throw new IllegalArgumentException("length must be > 0");

        char[] out = new char[length];
        long n = number;

        for (int i = length - 1; i >= 0; i--) {
            out[i] = BASE62_CHARACTERS.charAt((int) (n % BASE));
            n /= BASE;
        }

        if (n > 0) {
            throw new IllegalArgumentException("Number " + number + " does not fit in " + length + " base62 chars");
        }

        return new String(out);
    }
}