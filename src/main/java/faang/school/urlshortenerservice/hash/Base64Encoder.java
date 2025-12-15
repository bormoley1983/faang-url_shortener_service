package faang.school.urlshortenerservice.hash;

import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class Base64Encoder {

    //TODO сделать формирование безопасного кеша
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

        return base64.substring(0, 6); // берём первые 6 символов
    }
}
