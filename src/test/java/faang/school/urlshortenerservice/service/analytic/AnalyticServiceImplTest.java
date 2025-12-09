package faang.school.urlshortenerservice.service.analytic;


import faang.school.urlshortenerservice.model.Analytic;
import faang.school.urlshortenerservice.model.Url;
import faang.school.urlshortenerservice.repository.AnalyticRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class AnalyticServiceImplTest {
    @Mock
    private AnalyticRepository analyticRepository;
    @InjectMocks
    private AnalyticServiceImpl analyticService;

    @Test
    void recordClickAsync_ShouldNotSave_WhenIpAndUserAgentEmpty() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("");

        analyticService.recordClickAsync(new Url(), request);

        verify(analyticRepository, never()).save(any());
    }

    @Test
    void recordClickAsync_ShouldSave_WhenIpOrUserAgentPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Chrome");

        Url url = new Url();

        analyticService.recordClickAsync(url, request);

        ArgumentCaptor<Analytic> captor = ArgumentCaptor.forClass(Analytic.class);
        verify(analyticRepository).save(captor.capture());

        assertTrue(captor.getValue() instanceof Analytic);
    }
}