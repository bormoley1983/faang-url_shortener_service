package faang.school.urlshortenerservice.exception;

import faang.school.urlshortenerservice.dto.ErrorResponse;
import feign.FeignException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class UrlExceptionHandler {

    /**
     * Обработка исключения, когда URL не найден по хешу.
     * Возвращает 404 NOT FOUND.
     */
    @ExceptionHandler(UrlNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUrlNotFound(UrlNotFoundException ex) {
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
    public ErrorResponse handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() == null
                                ? "Invalid value"
                                : fe.getDefaultMessage(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        log.warn("Validation failed (request body): {}", details.keySet());

        return new ErrorResponse("validation_error", "Validation failed", details);
    }

    /**
     * Обработка ошибок валидации параметров запроса
     * (@RequestParam, @PathVariable).
     * <p>
     * Пример:
     * - пустой hash
     * - hash из пробелов
     * <p>
     * В details возвращается map:
     * propertyPath -> validationMessage
     * <p>
     * HTTP статус: 400 BAD REQUEST
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> details = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        this::extractPath,
                        v -> v.getMessage() == null ? "Invalid value" : v.getMessage(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        log.warn("Validation failed (request params): {}", details.keySet());

        return new ErrorResponse("validation_error", "Validation failed", details);
    }

    /**
     * Ошибки внешних сервисов (user-service, project-service).
     * - либо 500 Internal Server Error (упрощённый вариант)
     */
    @ExceptionHandler(FeignException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleFeignException(FeignException ex) {
        log.error("External service error. status={}", ex.status(), ex);

        return new ErrorResponse("external_service_error", "External service error occurred");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse("bad_request", ex.getMessage());
    }

    /**
     * Дефолтный обработчик всех исключений, не попавших под более специфичные обработчики.
     * Возвращает 500 INTERNAL SERVER ERROR и сообщение общего формата.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAny(Exception ex) {
        log.error("Internal server error", ex);
        return new ErrorResponse("internal_error", "Internal server error");
    }

    /**
     * Извлекает путь параметра из ConstraintViolation.
     * <p>
     * Пример:
     * - getOriginalUrl.hash
     * - createShortUrl.arg0.url
     */
    private String extractPath(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() == null
                ? "value"
                : violation.getPropertyPath().toString();
    }
}