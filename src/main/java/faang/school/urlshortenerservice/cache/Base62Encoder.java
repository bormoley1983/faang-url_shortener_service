package faang.school.urlshortenerservice.cache;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Base62Encoder {

    private final String BASE_62_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public List<String> encodeNumbers(List<Long> numbers) {
        List<String> hashes = new ArrayList<>();
        for (Long number : numbers) {
            hashes.add(applyBase62Encoding(number));
        }
        return hashes;
    }

    public String applyBase62Encoding(Long number) {
        StringBuilder builder = new StringBuilder();
        while (number > 0) {
            builder.append(BASE_62_CHARACTERS.charAt((int) (number % BASE_62_CHARACTERS.length())));
            number /= BASE_62_CHARACTERS.length();
        }
        return builder.toString();
    }
}
