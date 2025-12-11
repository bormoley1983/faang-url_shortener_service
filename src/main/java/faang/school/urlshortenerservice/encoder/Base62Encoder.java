package faang.school.urlshortenerservice.encoder;

import java.util.List;

/**
 * Сервис для для кодирования чисел в base62-хеши.
 */
public interface Base62Encoder {
    /**
     * Кодирует список чисел в список base62-строк
     *
     * @param numbers список уникальных чисел
     * @return список уникальных base62-хэшей
     */
    List<String> encode(List<Long> numbers);
}
