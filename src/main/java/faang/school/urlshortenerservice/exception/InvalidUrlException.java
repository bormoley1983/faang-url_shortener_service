package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.exception.annotation.HttpStatusError;
import org.springframework.http.HttpStatus;

@HttpStatusError(
        value = HttpStatus.UNPROCESSABLE_ENTITY,
        message = "URL не валиден")
public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
