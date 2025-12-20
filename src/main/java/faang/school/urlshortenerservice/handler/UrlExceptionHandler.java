package faang.school.urlshortenerservice.handler;

import faang.school.urlshortenerservice.dto.ErrorResponse;
import faang.school.urlshortenerservice.exception.DataNotFoundException;
import faang.school.urlshortenerservice.exception.DataValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class UrlExceptionHandler {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException e) {
        log.error("DataNotFoundException: {}", e.getMessage(), e);
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<ErrorResponse> handleDataValidationException(DataValidationException e) {
        log.error("Validation failed: {}", e.getMessage(), e);
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e) {
        log.error("Validation failed: {}", e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        FieldError priorityError = fieldErrors.stream()
                .min((e1, e2) -> {
                    String code1 = e1.getCode();
                    String code2 = e2.getCode();

                    if ("NotBlank".equals(code1)) return -1;
                    if ("NotBlank".equals(code2)) return 1;

                    if ("Pattern".equals(code1)) return -1;
                    if ("Pattern".equals(code2)) return 1;

                    return 0;
                })
                .orElse(null);

        String message;
        if (priorityError != null) {
            message = priorityError.getField() + ": " + priorityError.getDefaultMessage();
        } else {
            message = "Validation failed";
        }

        return buildErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);
        return buildErrorResponse("An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(
                message,
                LocalDateTime.now(),
                status.value()
        );

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }
}
