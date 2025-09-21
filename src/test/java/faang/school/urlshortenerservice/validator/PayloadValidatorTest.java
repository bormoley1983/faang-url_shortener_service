package faang.school.urlshortenerservice.validator;

import faang.school.urlshortenerservice.exception.ValidationException;
import org.apache.commons.validator.routines.UrlValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PayloadValidatorTest {
    @InjectMocks
    private PayloadValidator payloadValidator;
    @Spy
    private UrlValidator urlValidator;

    private static final String CORRECT_URL = "http://www.google.com";
    private static final String INVALID_URL = "https:";

    @Test
    void positive_shouldValidateUrl() {
        assertDoesNotThrow(() -> payloadValidator.validateUrl(CORRECT_URL));
    }

    @Test
    void negative_whenUrlIncorrect_throwsError() {
        assertThrows(ValidationException.class,
                () -> payloadValidator.validateUrl(INVALID_URL));
    }
}