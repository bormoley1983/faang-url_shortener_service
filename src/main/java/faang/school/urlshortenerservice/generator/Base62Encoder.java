package faang.school.urlshortenerservice.generator;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@AllArgsConstructor
public class Base62Encoder {

    @Value("${base.chars}")
    private String baseChars;

    public List<String> encode(List<Long> numbers) {
        List<String> encodedStrings = new ArrayList<>();
        if (numbers == null || numbers.isEmpty()) {
            return encodedStrings;
        }

        for (Long number : numbers) {
            StringBuilder encodedString = new StringBuilder();
            long num = number;

            if (num == 0) {
                encodedString.append(baseChars.charAt(0));
            } else {
                while (num > 0) {
                    int remainder = (int) (num % baseChars.length());
                    encodedString.insert(0, baseChars.charAt(remainder));
                    num /= baseChars.length();
                }
            }
            encodedStrings.add(encodedString.toString());
        }
        return encodedStrings;
    }
}