package faang.school.urlshortenerservice.service.hashgenearator;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Base62Encoder {

    private final Map<Long, Character> base62Table = createBase62Table();

    public List<String> encode(List<Long> numbers) {
        return numbers.stream().map(number -> {
            StringBuilder hash = new StringBuilder();
            do {
                hash.append(base62Table.get(number % 62));
                number = number / 62;
            }
            while (number > 0);

            return hash.reverse().toString();
        }).toList();
    }

    private Map<Long, Character> createBase62Table() {
        Map<Long, Character> base62TableInner = new HashMap<>();
        int offset = 48; //0-9: ASCII - 48-57, base62 - 0-9
        for (long i = 0; i < 62; i++) {
            if (i == 10) {
                offset = 87; //a-z: ASCII - 97-122, base62 - 10-35
            }

            if (i == 36) {
                offset = 29; //A-Z: ASCII - 65-90, base62 - 36-61
            }

            base62TableInner.put(i, (char) (i + offset));
        }

        return base62TableInner;
    }
}
