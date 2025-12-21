package faang.school.urlshortenerservice.exception;


import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class UrlExceptionHandler {

    private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_STATUS_MAP =
            Map.ofEntries(
                    Map.entry(MethodArgumentNotValidException.class, HttpStatus.BAD_REQUEST),
                    Map.entry(BindException.class, HttpStatus.BAD_REQUEST),
                    Map.entry(IllegalArgumentException.class, HttpStatus.BAD_REQUEST),
                    Map.entry(NoFreeHashesException.class, HttpStatus.SERVICE_UNAVAILABLE),
                    Map.entry(EntityNotFoundException.class, HttpStatus.NOT_FOUND),
                    Map.entry(UrlShortenerException.class, HttpStatus.INTERNAL_SERVER_ERROR),
                    Map.entry(RuntimeException.class, HttpStatus.INTERNAL_SERVER_ERROR)
            );

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleException(Exception e, HttpServletRequest rq) {
        HttpStatus status = EXCEPTION_STATUS_MAP.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        String message = buildMessage(e, status);
        log.error("[{} {}] -> {}", rq.getMethod(), rq.getRequestURL(), e.getMessage(), e);

        return new ErrorResponse(
                LocalDateTime.now(),
                rq.getRequestURL().toString(),
                e.getClass().getSimpleName(),
                message,
                status.value()
        );
    }

    private String buildMessage(Exception ex, HttpStatus status) {
        if (status.is4xxClientError()) {
            if (ex instanceof MethodArgumentNotValidException e) {
                return e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .collect(Collectors.joining("; "));
            }
            return ex.getMessage();
        }

        return "Internal server error";
    }
}
