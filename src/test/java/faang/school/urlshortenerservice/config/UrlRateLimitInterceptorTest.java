package faang.school.urlshortenerservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlRateLimitInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter responseBody;

    private UrlRateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() throws Exception {
        responseBody = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        lenient().when(request.getRequestURI()).thenReturn("/url");
        lenient().when(request.getRemoteAddr()).thenReturn("10.1.2.3");
    }

    @Test
    void preHandle_shouldAllowRequests_whenBelowLimit() throws Exception {
        interceptor = new UrlRateLimitInterceptor(5, 60);

        for (int i = 0; i < 5; i++) {
            assertTrue(interceptor.preHandle(request, response, new Object()));
        }
    }

    @Test
    void preHandle_shouldRejectRequest_whenLimitExceeded() throws Exception {
        interceptor = new UrlRateLimitInterceptor(2, 60);

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertFalse(interceptor.preHandle(request, response, new Object()));

        // Mockito mocks don't retain state; verify the calls were made with correct values
        org.mockito.Mockito.verify(response).setStatus(429);
        org.mockito.Mockito.verify(response).setContentType("application/json");
        assertEquals("{\"error\":\"Too many requests\"}", responseBody.toString());
    }

    @Test
    void preHandle_shouldUseFirstXForwardedForEntry_whenHeaderPresent() throws Exception {
        interceptor = new UrlRateLimitInterceptor(1, 60);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7, 10.0.0.1");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertFalse(interceptor.preHandle(request, response, new Object()));

        // A different client (different XFF first entry) must have its own counter.
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.8");
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_shouldIgnoreBlankXForwardedFor_whenHeaderBlank() throws Exception {
        interceptor = new UrlRateLimitInterceptor(1, 60);
        when(request.getHeader("X-Forwarded-For")).thenReturn("   ");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertFalse(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void preHandle_shouldResetCounter_whenWindowElapsed() throws Exception {
        interceptor = new UrlRateLimitInterceptor(1, 0); // zero-length window: every call is a new window

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
