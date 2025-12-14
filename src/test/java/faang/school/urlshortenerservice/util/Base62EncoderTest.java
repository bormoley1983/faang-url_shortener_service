package faang.school.urlshortenerservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class Base62EncoderTest {
    @Test
    void testEncodePadded() {
        assertEquals("000000", Base62Encoder.encodePadded(0, 6));
        assertEquals("000001", Base62Encoder.encodePadded(1, 6));
        assertEquals("00000a", Base62Encoder.encodePadded(36, 6));
        assertEquals("00000z", Base62Encoder.encodePadded(61, 6));
        assertEquals("000010", Base62Encoder.encodePadded(62, 6));
    }

    @Test
    void testEncodePadded_differentLength() {
        assertEquals("01", Base62Encoder.encodePadded(1, 2));
        assertEquals("1", Base62Encoder.encodePadded(1, 1));
    }
}