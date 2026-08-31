package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.exception.InvalidUrlException;
import faang.school.urlshortenerservice.exception.UrlExpiredException;
import faang.school.urlshortenerservice.exception.UrlExceptionHandler;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @Test
    void generateShortUrl_shouldReturnOkWithShortUrl_whenValidRequest() throws Exception {
        when(urlService.generateShortUrl("https://example.com")).thenReturn("http://localhost:18080/url/abc123");

        mockMvc.perform(post("/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("http://localhost:18080/url/abc123"));
    }

    @Test
    void generateShortUrl_shouldReturnBadRequest_whenUrlBlank() throws Exception {
        mockMvc.perform(post("/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validation failed: url: url must not be blank"));
    }

    @Test
    void generateShortUrl_shouldReturnBadRequest_whenServiceRejectsUrl() throws Exception {
        when(urlService.generateShortUrl("ftp://example.com"))
                .thenThrow(new InvalidUrlException("Only http and https URLs are allowed"));

        mockMvc.perform(post("/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"ftp://example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only http and https URLs are allowed"));
    }

    @Test
    void getUrlByHash_shouldReturnFoundWithLocation_whenUrlExists() throws Exception {
        when(urlService.getUrl("abc123")).thenReturn("https://example.com/landing");

        mockMvc.perform(get("/url/abc123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/landing"));
    }

    @Test
    void getUrlByHash_shouldReturnNotFound_whenServiceThrowsNotFound() throws Exception {
        when(urlService.getUrl("missing")).thenThrow(new UrlNotFoundException("missing"));

        mockMvc.perform(get("/url/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUrlByHash_shouldReturnGone_whenUrlExpired() throws Exception {
        when(urlService.getUrl("expired")).thenThrow(new UrlExpiredException("expired"));

        mockMvc.perform(get("/url/expired"))
                .andExpect(status().isGone());
    }

    @Test
    void getUrlByHash_shouldReturn500_whenUnexpectedFailure() throws Exception {
        when(urlService.getUrl("boom")).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(get("/url/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal server error"));
    }
}
