package faang.school.urlshortenerservice.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Base62EncoderTest {

    private Base62Encoder base62Encoder;

    @BeforeEach
    void setUp() {
        base62Encoder = new Base62Encoder();
    }

    @ParameterizedTest
    @CsvSource({
            "0, a",
            "1, b",
            "10, k",
            "61, 9",
            "62, ba",
            "123, b9",
            "3844, 100",
            "238327, b9",
            "999999, 4c92",
            "1000000, 4c93"
    })

    @ValueSource(longs = {1000L, 10000L, 100000L, 1000000L, 10000000L})
    void encodeToBase62_largeNumbers_returnsValidEncoding(long number) {
        String result = base62Encoder.encodeToBase62(number);
        assertThat(result).isNotEmpty();

        String validCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (char c : result.toCharArray()) {
            assertThat(validCharacters.indexOf(c)).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void encodeToBase62_maxLongValue_returnsEncodedString() {
        String result = base62Encoder.encodeToBase62(Long.MAX_VALUE);
        assertThat(result).isNotEmpty();

        assertThat(result).isEqualTo("k9viXaIfiWh");
    }

    @Test
    void encodeToBase62_sequenceNumbers_returnsIncrementalEncoding() {
        String prev = base62Encoder.encodeToBase62(0);
        for (int i = 1; i <= 100; i++) {
            String current = base62Encoder.encodeToBase62(i);

            assertThat(current).isNotEqualTo(prev);
            prev = current;
        }
    }
}