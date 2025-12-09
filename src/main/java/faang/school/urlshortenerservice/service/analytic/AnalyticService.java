package faang.school.urlshortenerservice.service.analytic;

import faang.school.urlshortenerservice.model.Url;
import jakarta.servlet.http.HttpServletRequest;

public interface AnalyticService {
    void recordClickAsync(Url url, HttpServletRequest request);
}
