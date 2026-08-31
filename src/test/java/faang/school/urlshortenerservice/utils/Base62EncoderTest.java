package faang.school.urlshortenerservice.utils;

import faang.school.urlshortenerservice.util.Base62Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class Base62EncoderTest {

    private Base62Encoder base62Encoder;

    @BeforeEach
    void setUp() {
        base62Encoder = new Base62Encoder();
    }

    @Test
    void shouldEncodeValidInput() {
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);

        List<String> result = base62Encoder.encode(numbers);

        assertEquals(Arrays.asList("B", "C", "D"), result);
    }

    @Test
    void shouldThrowExceptionForEmptyList() {
        List<Long> emptyList = Collections.emptyList();

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                base62Encoder.encode(emptyList)
        );
        assertTrue(exception.getMessage().contains("Supplied list of numbers is empty!"));
    }

    @Test
    void shouldThrowExceptionForDuplicateValues() {
        List<Long> duplicateList = Arrays.asList(1L, 2L, 1L);

        assertThrows(IllegalArgumentException.class, () ->
                base62Encoder.encode(duplicateList)
        );
    }

    @Test
    void encode_shouldEncodeZeroAsA() {
        assertEquals(List.of("A"), base62Encoder.encode(List.of(0L)));
    }

    @Test
    void encode_shouldEncodeBaseBoundaryValues() {
        // alphabet is A-Z (0-25), a-z (26-51), 0-9 (52-61): 61 -> "9", 62 -> "BA"
        assertEquals(List.of("9", "BA"), base62Encoder.encode(List.of(61L, 62L)));
    }

    @Test
    void encode_shouldRoundTripWithDecode() {
        List<Long> numbers = Arrays.asList(0L, 1L, 61L, 62L, 3844L, 238328L);

        List<String> encoded = base62Encoder.encode(numbers);

        for (int i = 0; i < numbers.size(); i++) {
            assertEquals(numbers.get(i), decode(encoded.get(i)), "round-trip failed for " + numbers.get(i));
        }
    }

    @Test
    void encodeFixed_shouldPadWithLeadingA() {
        assertEquals("AAAAAA", base62Encoder.encodeFixed(0L));
        assertEquals("AAAAAB", base62Encoder.encodeFixed(1L));
    }

    @Test
    void encodeFixed_shouldThrow_whenNumberDoesNotFit() {
        // 62^6 = 56,800,235,584 is the first value that does not fit in 6 chars
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> base62Encoder.encodeFixed(56_800_235_584L));

        assertTrue(exception.getMessage().contains("does not fit in 6 base62 chars"));
    }

    @Test
    void encodeFixed_shouldEncodeMaximumSupportedValue() {
        // 62^6 - 1 = 56,800,235,583 fits exactly in 6 chars; last alphabet char is "9"
        assertEquals("999999", base62Encoder.encodeFixed(56_800_235_583L));
    }

    @Test
    void encodeFixedList_shouldThrow_whenEmpty() {
        assertThrows(IllegalArgumentException.class, () -> base62Encoder.encodeFixed(List.of()));
    }

    @Test
    void encodeFixedList_shouldThrow_whenDuplicates() {
        assertThrows(IllegalArgumentException.class, () -> base62Encoder.encodeFixed(Arrays.asList(1L, 1L)));
    }

    private static Long decode(String value) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        long result = 0;
        for (char c : value.toCharArray()) {
            result = result * 62 + alphabet.indexOf(c);
        }
        return result;
    }
}