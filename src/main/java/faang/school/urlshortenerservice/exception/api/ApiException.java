package faang.school.urlshortenerservice.exception.api;

import faang.school.urlshortenerservice.exception.LoggableException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends LoggableException {
    private final HttpStatus status;

    protected ApiException(String message, String debugMessage) {
        super(message);
        this.debugMessage = debugMessage;
        this.status = getDefaultStatus();
    }

    protected abstract HttpStatus getDefaultStatus();
}
