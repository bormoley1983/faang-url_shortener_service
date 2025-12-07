package faang.school.urlshortenerservice.encoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class Base62EncoderImpl implements Base62Encoder {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();

    @Override
    public List<String> encode(List<Long> numbers) {
        List<String> result = new ArrayList<>(numbers.size());
        for (Long number : numbers) {
            if (number == null || number < 0) {
                log.error("Number must be non-negative, number: {}, data: {}", number, numbers);
                throw new IllegalArgumentException("Number must be non-null and non-negative");
            }
            result.add(encodeBase62(number));
        }
        return result;
    }

    private String encodeBase62(Long number) {
        if (number == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        long copy = number;

        while (copy > 0) {
            int remainder = (int) (copy % BASE);
            sb.append(ALPHABET.charAt(remainder));
            copy /= BASE;
        }

        return sb.reverse().toString();
    }
}