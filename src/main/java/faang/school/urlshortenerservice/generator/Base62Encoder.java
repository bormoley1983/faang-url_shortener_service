package faang.school.urlshortenerservice.generator;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private final static String BASE_62_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final static String ZERO_CHAR_BASE62 = String.valueOf(BASE_62_CHARACTERS.charAt(0));

    public String encodeToBase62(long number) {
        if (number <= 0) {
            return ZERO_CHAR_BASE62;
        }
        StringBuilder encode = new StringBuilder();
        while (number > 0) {
            long remnant = number % BASE_62_CHARACTERS.length();
            char symbol = BASE_62_CHARACTERS.charAt((int) remnant);
            encode.append(symbol);
            number = number / BASE_62_CHARACTERS.length();
        }
        return encode.reverse().toString();
    }
}