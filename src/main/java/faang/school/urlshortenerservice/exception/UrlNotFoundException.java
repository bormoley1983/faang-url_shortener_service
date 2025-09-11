package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.exception.annotation.HttpStatusError;
import org.springframework.http.HttpStatus;

@HttpStatusError(
        value = HttpStatus.NOT_FOUND,
        message = "URL не существует")
public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String message) {
        super(message);
    }
}
