package faang.school.urlshortenerservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Base62EncoderTest {

    private Base62Encoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new Base62Encoder();
    }

    @Test
    void testEncodeEmptyList() {
        List<Long> emptyList = Collections.emptyList();

        List<String> result = encoder.encode(emptyList);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEncodeNullInput() {
        assertThrows(NullPointerException.class, () -> encoder.encode(null));
    }

    @Test
    void testEncodeListWithNullElement() {
        List<Long> listWithNull = Arrays.asList(1L, null, 2L);

        assertThrows(IllegalArgumentException.class, () -> encoder.encode(listWithNull));
    }

    @Test
    void testEncodeNegativeNumber() {
        List<Long> listWithNegativeNumber = Arrays.asList(-1L, 10L, 100L);

        assertThrows(IllegalArgumentException.class, () -> encoder.encode(listWithNegativeNumber));
    }

    @Test
    void testEncodeMultipleNumbers() {
        List<Long> numbers = Arrays.asList(0L, 1L, 62L, 123L, 238327L);
        List<String> expected = Arrays.asList("0", "1", "10", "1Z", "ZZZ");

        List<String> result = encoder.encode(numbers);

        assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), result.get(i));
        }
    }

    @Test
    void testEncodeZero() {
        List<Long> numbers = Collections.singletonList(0L);

        List<String> result = encoder.encode(numbers);

        assertEquals(1, result.size());
        assertEquals("0", result.get(0));
    }
}