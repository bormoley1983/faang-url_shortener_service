package faang.school.urlshortenerservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class UrlExceptionHandler {

    private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_STATUS_MAP = Map.ofEntries(
            Map.entry(IllegalArgumentException.class, HttpStatus.BAD_REQUEST),
            Map.entry(ConstraintViolationException.class, HttpStatus.BAD_REQUEST),
            Map.entry(UrlNotFoundException.class, HttpStatus.NOT_FOUND)
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

    private String buildMessage(Exception e, HttpStatus status) {
        if (status.is4xxClientError()) {
            return e.getMessage();
        }
        return "Internal server error";
    }

}


