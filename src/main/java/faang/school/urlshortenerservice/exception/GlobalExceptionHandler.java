package faang.school.urlshortenerservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обработчик исключений REST-контроллера для различных типов ошибок, возникающих при выполнении запросов.
 *
 * <p>Каждый метод обрабатывает конкретный тип исключения и возвращает клиенту
 * соответствующий HTTP-код ответа и сообщение об ошибке.
 * </p>
 *
 * <p>Обрабатываемые исключения:
 * <ul>
 *     <li>{@link MethodArgumentNotValidException} — ошибки валидации данных (@Valid), возвращает {@code 400 Bad Request}</li>
 *     <li>{@link UrlNotFoundException} — ресурс не найден, возвращает {@code 404 Not Found}</li>
 *     <li>{@link Exception} — любые другие необработанные исключения, возвращает {@code 500 Internal Server Error}</li>
 * </ul>
 * </p>
 */

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> hashNotFoundException(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException", ex);
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String urlNotFoundException(UrlNotFoundException ex) {
        log.error("UrlNotFoundException", ex);
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> hashNotFoundException(Exception ex) {
        log.error("Exception", ex);
       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
