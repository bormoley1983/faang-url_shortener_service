package faang.school.urlshortenerservice.exception;

import lombok.Getter;

@Getter
public abstract class LoggableException extends RuntimeException {
    protected String debugMessage;

    protected LoggableException(String message) {
        super(message);
        this.debugMessage = message;
    }
}
