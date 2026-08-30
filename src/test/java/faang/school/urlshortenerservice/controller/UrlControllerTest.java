package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.exception.UrlExceptionHandler;
import faang.school.urlshortenerservice.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {
    @Mock
    private UrlService urlService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UrlController(urlService))
                .setControllerAdvice(new UrlExceptionHandler())
                .build();
    }

    @Test
    void generateShortUrl_shouldReturnBadRequest_whenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Request body must contain valid JSON with a url field"));
    }

    @Test
    void generateShortUrl_shouldReturnUnsupportedMediaType_whenBodyIsPlainText() throws Exception {
        mockMvc.perform(post("/url")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("https://example.com"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().string("Content-Type must be application/json"));
    }
}
