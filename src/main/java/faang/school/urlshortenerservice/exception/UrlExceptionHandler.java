package faang.school.urlshortenerservice.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * UrlExceptionHandler — централизованный обработчик ошибок для REST-контроллеров
 * <p>
 * Класс перехватывает исключение, возникающее в процессе работы контроллеров,
 * и преобразует их в стандартизированные HTTP-ответы с соответствующими кодами состояния.
 * </p>
 *
 * <p>Обработчик поддерживает следующие типы исключений:</p>
 * <ul>
 *     <li>{@link MethodArgumentNotValidException} - ошибки валидации данных (при использовании @Valid), возвращает
 *     {@code 400 Bad Request}</li>
 *     <li>{@link EntityNotFoundException} - ресурс не найден, возвращает {@code  400 NOT FOUND}</li>
 *     <li>{@link InternalError} - другие необработанные ошибки, возвращает {@code 500 Internal Server Error}</li>
 * </ul>
 *
 * @author Linempy
 * @since 16.09.2025
 */
@Slf4j
@RestControllerAdvice
public class UrlExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBadRequest(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException e) {
        log.error("EntityNotFoundException", e);
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleInternalError(Exception e) {
        log.error("InternalError", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}