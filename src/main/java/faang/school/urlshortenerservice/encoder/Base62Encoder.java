package faang.school.urlshortenerservice.encoder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Base62Encoder implements BaseEncoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();

    @Override
    public List<String> encode(List<Long> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            log.warn("Attempted to encode empty or null list");
            return new ArrayList<>();
        }
        log.debug("Encoding {} numbers to Base62", numbers.size());

        List<String> hashes = new ArrayList<>(numbers.size());

        for (Long number : numbers) {
            String hash = encodeToString(number);
            hashes.add(hash);
        }
        log.debug("Successfully encoded {} numbers to Base62 hashes", hashes.size());
        return hashes;
    }

    private String encodeToString(Long number) {
        if (number == null || number < 0) {
            log.warn("Invalid number for encoding: {}", number);
            throw new IllegalArgumentException("Number must be non-negative");
        }
        if (number == 0) {
            return "0";
        }

        StringBuilder encoded = new StringBuilder();
        long temp = number;

        while (temp > 0) {
            int remainder = (int) (temp % BASE);
            char symbol = BASE62_CHARS.charAt(remainder);
            encoded.insert(0, symbol);
            temp = temp / BASE;
        }
        return encoded.toString();
    }
}
