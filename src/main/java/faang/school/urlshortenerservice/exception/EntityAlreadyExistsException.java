package faang.school.urlshortenerservice.exception;

import org.slf4j.helpers.MessageFormatter;

public class EntityAlreadyExistsException extends BadRequestException {
    public EntityAlreadyExistsException(String messagePattern, Object... argArray) {
        super(MessageFormatter.arrayFormat(messagePattern, argArray).getMessage());
    }
}
