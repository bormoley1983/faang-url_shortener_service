package faang.school.urlshortenerservice.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler — централизованный обработчик исключений для REST-контроллеров.
 *
 * <p>Обрабатывает различные исключения, возникающие во время выполнения запросов, и возвращает
 * клиенту стандартизированные ответы с информацией об ошибках в формате JSON.
 * Это помогает унифицировать обработку ошибок и улучшить качество взаимодействия API с клиентами.</p>
 *
 * <p>В данном классе реализованы обработчики для:
 * <ul>
 *     <li>Случаев, когда ресурс не найден (EntityNotFoundException)</li>
 *     <li>Ошибок валидации данных запроса (BadRequestException)</li>
 *     <li>И любых других необработанных исключений (Exception)</li>
 * </ul>
 * </p>
 *
 * <p>Каждый обработчик возвращает строку с сообщением ошибки</p>
 *
 * @author mrnght
 * @since 10.09.2025
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("EntityNotFoundException", e);
        return e.getMessage();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(BadRequestException e) {
        log.error("BadRequestException", e);
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e) {
        log.error("Exception", e);
        return e.getMessage();
    }
}
