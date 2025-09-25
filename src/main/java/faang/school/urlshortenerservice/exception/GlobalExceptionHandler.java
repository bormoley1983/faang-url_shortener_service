package faang.school.urlshortenerservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Обрабатывает ситуацию, когда короткий URL не найден в системе.
     *
     * @param e       исключение UrlNotFoundException
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 404 и описанием ошибки
     */
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFoundException(UrlNotFoundException e, HttpServletRequest request) {
        log.warn("URL not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(HttpStatus.NOT_FOUND,
                        "URL Not Found",
                        e.getMessage(),
                        request.getRequestURI()));
    }

    /**
     * Обрабатывает ситуацию, когда в системе нет доступных хэшей
     * для генерации коротких ссылок.
     *
     * @param e       исключение HashNotAvailableException
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 503 и описанием ошибки
     */
    @ExceptionHandler(HashNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleHashNotAvailableException(HashNotAvailableException e, HttpServletRequest request) {
        log.error("Hash not available: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                        "Service Unavailable",
                        "Unable to generate short URL at this time. Please try again later.",
                        request.getRequestURI()));
    }

    /**
     * Обрабатывает ошибки валидации тела запроса (например, некорректный формат URL).
     *
     * @param e       исключение MethodArgumentNotValidException
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 400 и списком ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation error: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST,
                        "Validation Failed",
                        "Request validation failed",
                        request.getRequestURI(),
                        fieldErrors));
    }

    /**
     * Обрабатывает ошибки валидации параметров методов (например, при использовании @Valid в параметрах контроллера).
     *
     * @param e       исключение ConstraintViolationException
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 400 и описанием ошибки
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.warn("Constraint violation: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST,
                        "Invalid Request Parameter",
                        e.getMessage(),
                        request.getRequestURI()));
    }

    /**
     * Обрабатывает ошибки, возникающие из-за несовпадения типов аргументов методов контроллеров.
     *
     * @param e       исключение MethodArgumentTypeMismatchException
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 400 и описанием ошибки
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("Method argument type mismatch: {}", e.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                e.getValue(), e.getName(), e.getRequiredType().getSimpleName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(HttpStatus.BAD_REQUEST,
                        "Invalid Parameter Type",
                        message,
                        request.getRequestURI()));
    }

    /**
     * Обрабатывает ошибки, связанные с некорректными аргументами.
     * Например, отсутствие обязательного заголовка {@code x-user-id}.
     *
     * @param e       исключение IllegalArgumentException
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 400 или 500 (в зависимости от контекста)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal argument: {}", e.getMessage());

        HttpStatus status = e.getMessage().contains("header")
                ? HttpStatus.BAD_REQUEST
                : HttpStatus.INTERNAL_SERVER_ERROR;

        String error = status == HttpStatus.BAD_REQUEST ? "Bad Request" : "Internal Server Error";

        return ResponseEntity.status(status)
                .body(buildErrorResponse(status, error, e.getMessage(), request.getRequestURI()));
    }

    /**
     * Обрабатывает все необработанные ранее исключения,
     * включая ошибки состояния приложения и системные сбои.
     *
     * @param e       исключение (IllegalStateException или любое другое)
     * @param request исходный HTTP-запрос
     * @return HTTP-ответ со статусом 500 и общим сообщением об ошибке
     */
    @ExceptionHandler({IllegalStateException.class, Exception.class})
    public ResponseEntity<ErrorResponse> handleGenericExceptions(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred. Please try again later.",
                        request.getRequestURI()));
    }

    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }

    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            String path
    ) {
        return buildErrorResponse(status, error, message, path, null);
    }
}
