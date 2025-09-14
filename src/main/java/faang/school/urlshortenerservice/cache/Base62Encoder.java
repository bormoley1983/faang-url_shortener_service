package faang.school.urlshortenerservice.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Класс для кодирования списка уникальных возрастающих чисел по алгоритму base62
 */
@Slf4j
@Component
public class Base62Encoder {

    private final String BASE_62_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Применяет кодировку к каждому числу в списке
     * @param numbers список уникальных чисел
     * @return список уникальных строк - хэшей
     */
    public List<String> encodeNumbers(List<Long> numbers) {
        return numbers.stream()
                .map(this::applyBase62Encoding)
                .toList();
    }

    /**
     * Делает из числа уникальный хэш по алгоритму base62
     * @param number входное число
     * @return строка - уникальный хэш
     */
    public String applyBase62Encoding(Long number) {
        StringBuilder builder = new StringBuilder();
        while (number > 0) {
            builder.append(BASE_62_CHARACTERS.charAt((int) (number % BASE_62_CHARACTERS.length())));
            number /= BASE_62_CHARACTERS.length();
        }
        return builder.toString();
    }
}
