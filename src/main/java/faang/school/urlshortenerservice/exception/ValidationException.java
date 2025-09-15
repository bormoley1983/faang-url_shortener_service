package faang.school.urlshortenerservice.exception;

import org.slf4j.helpers.MessageFormatter;

public class ValidationException extends BadRequestException {
    public ValidationException(String messagePattern, Object... argArray) {
        super(MessageFormatter.arrayFormat(messagePattern, argArray).getMessage());
    }
}
