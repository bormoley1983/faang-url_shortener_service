package faang.school.urlshortenerservice.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UrlBuilderServiceImplTest {
    @InjectMocks
    private UrlBuilderServiceImpl urlBuilderService;

    @BeforeEach
    void setUp() {
        urlBuilderService = new UrlBuilderServiceImpl();
        ReflectionTestUtils.setField(urlBuilderService, "host", "https://short.ly");
    }

    @Test
    void buildShortUrl_ShouldReturnCorrectUrl_WhenGivenHash() {
        String hash = "abc123";

        String result = urlBuilderService.buildShortUrl(hash);

        assertEquals("https://short.ly/abc123", result);
    }

    @Test
    void buildShortUrl_ShouldReturnDifferentUrl_WhenGivenAnotherHash() {
        String hash = "xyz789";

        String result = urlBuilderService.buildShortUrl(hash);

        assertEquals("https://short.ly/xyz789", result);
    }
}