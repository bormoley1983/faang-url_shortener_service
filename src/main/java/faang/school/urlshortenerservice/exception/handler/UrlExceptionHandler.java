package faang.school.urlshortenerservice.exception.handler;

import faang.school.urlshortenerservice.exception.DataValidationException;
import faang.school.urlshortenerservice.exception.InternalServerError;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class UrlExceptionHandler {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(InternalServerError.class)
    public ErrorResponse handleInternalServerError(InternalServerError e) {
        log.error("Internal server error: {}", e.getMessage());
        return ErrorResponse.builder(
                        e,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataValidationException.class)
    public ErrorResponse handleDataValidationException(DataValidationException e) {
        log.error("Data validation error: {}", e.getMessage());
        return ErrorResponse.builder(
                        e,
                        HttpStatus.BAD_REQUEST,
                        e.getMessage())
                .build();
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UrlNotFoundException.class)
    public ErrorResponse handleUrlNotFoundException(UrlNotFoundException e) {
        log.error("Url not found: {}", e.getMessage());
        return ErrorResponse.builder(
                        e,
                        HttpStatus.NOT_FOUND,
                        e.getMessage())
                .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        return ErrorResponse.builder(
                        e,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage())
                .build();
    }
}
