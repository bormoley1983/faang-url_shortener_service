package faang.school.urlshortenerservice.hash;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class Base64Encoder {

    public List<String> encode(List<Long> numbers) {
        return numbers.stream()
                .takeWhile(n -> n > 0)
                .map(this::encodeLong)
                .toList();
    }

    private String encodeLong(Long value) {
        byte[] bytes = new byte[8];

        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        String base64 = Base64.getEncoder().withoutPadding().encodeToString(bytes);

        return base64.substring(0, 6);
    }
}
