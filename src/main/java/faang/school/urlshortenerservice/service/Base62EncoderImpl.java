package faang.school.urlshortenerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class Base62EncoderImpl implements Base62Encoder {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;
    private static final int HASH_LEN = 6;

    @Override
    public List<String> encode(List<Long> numbers) {
        return numbers.stream()
                .map(this::toBase62)
                .map(this::leftPadToSix)
                .toList();
    }

    private String toBase62(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative: " + value);
        }
        if (value == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        long x = value;
        while (x > 0) {
            int idx = (int) (x % BASE);
            sb.append(ALPHABET.charAt(idx));
            x /= BASE;
        }
        return sb.reverse().toString();
    }

    private String leftPadToSix(String raw) {
        if (raw.length() > HASH_LEN) {
            throw new IllegalStateException("Hash length > " + HASH_LEN + ": " + raw);
        }
        return "0".repeat(HASH_LEN - raw.length()) + raw;
    }
}