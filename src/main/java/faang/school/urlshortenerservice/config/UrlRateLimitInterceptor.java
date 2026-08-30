package faang.school.urlshortenerservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * in-process sliding-window rate limiter for the public shortening/redirect surface.
 *
 * <p>Keyed by client IP so a single caller cannot flood the service. This is a first line of
 * defense inside the service; edge-level limits (gateway/WAF) and abuse response remain
 * deployment concerns. The window state is bounded by the number of distinct active clients,
 * which is acceptable for this service's scale.
 */
@Component
@Slf4j
public class UrlRateLimitInterceptor implements HandlerInterceptor {

    private final Clock clock;
    private final int maxRequestsPerWindow;
    private final long windowMillis;

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public UrlRateLimitInterceptor(
            @Value("${shortener.rate-limit.max-requests:100}") int maxRequestsPerWindow,
            @Value("${shortener.rate-limit.window-seconds:60}") long windowSeconds) {
        this.clock = Clock.systemUTC();
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.windowMillis = windowSeconds * 1000L;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = resolveClientIp(request);
        long now = clock.millis();

        WindowCounter counter = counters.computeIfAbsent(clientIp, k -> new WindowCounter(now));
        synchronized (counter) {
            if (now - counter.windowStart >= windowMillis) {
                counter.windowStart = now;
                counter.count = 0;
            }
            counter.count++;
            if (counter.count > maxRequestsPerWindow) {
                log.warn("Rate limit exceeded for client {} on {}", clientIp, request.getRequestURI());
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests\"}");
                return false;
            }
        }
        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class WindowCounter {
        private long windowStart;
        private int count;

        private WindowCounter(long now) {
            this.windowStart = now;
            this.count = 0;
        }
    }
}
