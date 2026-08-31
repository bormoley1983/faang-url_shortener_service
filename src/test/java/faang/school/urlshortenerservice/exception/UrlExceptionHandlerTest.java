package faang.school.urlshortenerservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlExceptionHandlerTest {

    private final UrlExceptionHandler handler = new UrlExceptionHandler();

    @Test
    void handleInvalidUrlException_shouldReturnBadRequest() {
        ResponseEntity<String> response = handler.handleInvalidUrlException(
                new InvalidUrlException("bad url"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad url", response.getBody());
    }

    @Test
    void handleUrlNotFoundException_shouldReturnNotFound() {
        ResponseEntity<String> response = handler.handleUrlNotFoundException(
                new UrlNotFoundException("abc123"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleUrlExpiredException_shouldReturnGone() {
        ResponseEntity<String> response = handler.handleUrlExpiredException(
                new UrlExpiredException("abc123"));

        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }

    @Test
    void handleValidationExceptions_shouldJoinFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(
                new FieldError("target", "url", "url must not be blank"),
                new FieldError("target", "url", "url must not exceed 2048 characters")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<String> response = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed: url: url must not be blank, url: url must not exceed 2048 characters",
                response.getBody());
    }

    @Test
    void handleUnreadableRequest_shouldReturnBadRequest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<String> response = handler.handleUnreadableRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request body must contain valid JSON with a url field", response.getBody());
    }

    @Test
    void handleUnsupportedMediaType_shouldReturn415() {
        HttpMediaTypeNotSupportedException ex = mock(HttpMediaTypeNotSupportedException.class);

        ResponseEntity<String> response = handler.handleUnsupportedMediaType(ex);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("Content-Type must be application/json", response.getBody());
    }

    @Test
    void handleGenericException_shouldReturn500() {
        ResponseEntity<String> response = handler.handleGenericException(
                new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody());
    }
}
