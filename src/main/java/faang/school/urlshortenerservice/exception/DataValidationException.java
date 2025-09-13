package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.exception.annotation.HttpStatusError;
import org.springframework.http.HttpStatus;

@HttpStatusError(
        value = HttpStatus.BAD_REQUEST,
        message = "Ошибка данных")
public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }
}
