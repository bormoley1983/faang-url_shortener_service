package faang.school.urlshortenerservice.hash;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class Encoder {

    private static final String BASE_62_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\"";

    public List <String> encode (List<Long> numbers){
    return null;
    }

    public String applyBase62Encoding(long number) {
        StringBuilder sb = new StringBuilder();
        while (number > 0) {
            sb.append(BASE_62_CHARACTERS.charAt((int) (number % BASE_62_CHARACTERS.length())));
            number /= Encoder.BASE_62_CHARACTERS.length();
        }
        return sb.toString();
    }
}
