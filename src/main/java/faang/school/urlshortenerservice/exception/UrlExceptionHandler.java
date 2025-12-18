package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.dto.ErrorResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@RestControllerAdvice
public class UrlExceptionHandler {

    /**
     * Обработка исключения, когда URL не найден по хешу.
     * Возвращает 404 NOT FOUND.
     */
    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUrlNotFoundException(UrlNotFoundException ex) {
        log.warn("URL not found: {}", ex.getMessage());
        return new ErrorResponse("url_not_found", ex.getMessage());
    }

    /**
     * Обработка ошибок валидации входных данных.
     * Возвращает JSON с полями, которые не прошли валидацию, и сообщениями из аннотаций.
     * Возвращает 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for fields: {}",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getField)
                        .toList()
        );

        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (msg1, msg2) -> msg1
                ));
    }

    /**
     * Ошибки внешних сервисов (user-service, project-service).
     * Возвращает 500 Internal Server Error.
     */
    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleFeignException(FeignException ex) {
        log.error("External service error: {}", ex.getMessage());
        return new ErrorResponse("external_service_error", "External service error occurred");
    }

    /**
     * Внутренние ошибки приложения.
     * Любые необработанные RuntimeException → 500 Internal Server Error.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException ex) {
        log.error("Internal server error", ex);
        return new ErrorResponse("internal_error", "Internal server error");
    }
}