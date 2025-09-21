package faang.school.urlshortenerservice.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Base62Encoder {
    private static final String BASE62_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE62 = BASE62_CHARS.length();

    public List<String> encode(List<Long> nums) {
        return nums.stream()
                .map(this::encode)
                .toList();
    }

    public String encode(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62_CHARS.charAt((int) (num % BASE62)));
            num /= BASE62;
        }
        return sb.reverse().toString();
    }
}
