package faang.school.urlshortenerservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Base62Encoder {

    @Value("${base.chars}")
    private String baseChars;

    public List<String> encode(List<Long> numbers) {
        List<String> result = new ArrayList<>(numbers.size());
        for (Long number : numbers) {
            result.add(encode(number));
        }
        return result;
    }

    private String encode(long num) {
        if (num == 0) {
            return String.valueOf(baseChars.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int remainder = (int) (num % baseChars.length());
            sb.append(baseChars.charAt(remainder));
            num /= baseChars.length();
        }
        return sb.reverse().toString();
    }
}