package faang.school.urlshortenerservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Base62EncoderTest {
    private final Base62Encoder encoder = new Base62Encoder();

    @Test
    @DisplayName("Успешный расчет хэша для списка чисел")
    void positive_shouldEncodeListNums() {
        List<String> expect = List.of("b", "c", "d");

        List<String> actual = encoder.encode(List.of(1L, 2L, 3L));

        assertEquals(expect, actual);
    }

    @Test
    @DisplayName("Успешный расчет хэша для числа")
    void positive_shouldEncodeNum() {
        String expect = "b";

        String actual = encoder.encode(1);

        assertEquals(expect, actual);
    }
}