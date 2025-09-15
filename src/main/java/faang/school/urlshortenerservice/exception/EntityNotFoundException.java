package faang.school.urlshortenerservice.exception;

import org.slf4j.helpers.MessageFormatter;

public class EntityNotFoundException extends BadRequestException {
    public EntityNotFoundException(String messagePattern, Object... argArray) {
        super(MessageFormatter.arrayFormat(messagePattern, argArray).getMessage());
    }
}
