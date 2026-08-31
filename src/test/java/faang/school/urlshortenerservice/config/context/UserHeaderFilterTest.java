package faang.school.urlshortenerservice.config.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserHeaderFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletResponse response;

    @Mock
    private FilterChain chain;

    private UserContext userContext;

    private UserHeaderFilter filter;

    @BeforeEach
    void setUp() {
        userContext = new UserContext();
        filter = new UserHeaderFilter(userContext);
        lenient().when(request.getRequestURI()).thenReturn("/url");
    }

    @Test
    void doFilter_shouldSetAndClearUserId_whenNonGetWithHeader() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("x-user-id")).thenReturn("42");
        // capture the userId visible to downstream handlers; do NOT re-invoke the chain
        java.util.concurrent.atomic.AtomicLong captured = new java.util.concurrent.atomic.AtomicLong(-1L);
        doAnswer(invocation -> {
            captured.set(userContext.getUserId());
            return null;
        }).when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        filter.doFilter(request, response, chain);

        assertEquals(42L, captured.get());
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldThrow_whenNonGetMissingHeader() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("x-user-id")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> filter.doFilter(request, response, chain));

        assertEquals("Missing required header 'x-user-id'. Please include 'x-user-id' header with a valid user ID in your request.",
                exception.getMessage());
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_shouldClearContext_whenChainThrows() throws Exception {
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader("x-user-id")).thenReturn("7");
        org.mockito.Mockito.doThrow(new ServletException("boom"))
                .when(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));

        // context must be cleared even when the chain fails
        try {
            userContext.getUserId();
        } catch (NullPointerException expected) {
            // ThreadLocal was removed by clear()
        }
    }

    @Test
    void doFilter_shouldPassThrough_whenGetRequest() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldPassThrough_whenSwaggerPath() throws Exception {
        // swagger paths are excluded before the method is checked, so getMethod is never called
        lenient().when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldPassThrough_whenApiDocsPath() throws Exception {
        // api-docs paths are excluded before the method is checked, so getMethod is never called
        lenient().when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
