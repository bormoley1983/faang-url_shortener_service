package faang.school.urlshortenerservice.service.analytic;

import faang.school.urlshortenerservice.model.Analytic;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.AnalyticRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticServiceImpl implements AnalyticService {
    private final AnalyticRepository analyticRepository;

    @Override
    @Async("analyticExecutor")
    public void recordClickAsync(Url url, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        if ((ip == null || ip.isBlank()) && (userAgent == null || userAgent.isBlank())) {
            return;
        }

        analyticRepository.save(
                Analytic.builder()
                        .url(url)
                        .userAgent(userAgent)
                        .ipAddress(ip)
                        .build()
        );
    }
}
