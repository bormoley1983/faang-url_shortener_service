package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.exception.annotation.HttpStatusError;
import org.springframework.http.HttpStatus;

@HttpStatusError(
        value = HttpStatus.FORBIDDEN,
        message = "Нет доступа")
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
