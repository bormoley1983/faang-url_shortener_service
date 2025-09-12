package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.exception.annotation.HttpStatusError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

/**
 * GlobalExceptionHandler — класс обрабатывающий исключения возникающие в rest controller
 *
 * @author bozya
 * @since 10.09.2025
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyException(Exception ex) {
        HttpStatusError annotation = ex.getClass().getAnnotation(HttpStatusError.class);

        if (annotation != null) {
            return buildResponse(ex, annotation.value(), annotation.message());
        }

        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка");
    }

    private ResponseEntity<ErrorResponse> buildResponse(Exception ex, HttpStatus status, String message) {
        String finalMessage = !message.isBlank() ? message : ex.getMessage();

        ErrorResponse errorResponse = new ErrorResponse(finalMessage);
        return new ResponseEntity<>(errorResponse, status);
    }
}