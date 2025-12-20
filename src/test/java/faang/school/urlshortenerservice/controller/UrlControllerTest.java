package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.config.context.UserContext;
import faang.school.urlshortenerservice.dto.CreateUrlRequestDto;
import faang.school.urlshortenerservice.exception.UrlExceptionHandler;
import faang.school.urlshortenerservice.exception.UrlNotFoundException;
import faang.school.urlshortenerservice.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
@Import(UrlExceptionHandler.class)
@DisplayName("UrlController validation & exception handling")
class UrlControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserContext userContext;

    @MockBean
    private UrlService urlService;

    private static final String USER_ID_HEADER = "x-user-id";
    private static final String USER_ID = "1";

    // ------------------- VALIDATION -------------------

    @Test
    @DisplayName("POST /url → 400 + validation_error when URL is not valid HTTP URL")
    void createUrl_invalidUrl_returnsValidationErrorResponse() throws Exception {
        CreateUrlRequestDto dto = new CreateUrlRequestDto("not-a-url");

        mvc.perform(post("/url")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("validation_error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.url").exists());

        verifyNoInteractions(urlService);
    }

    @Test
    @DisplayName("POST /url → 400 + validation_error when URL is blank")
    void createUrl_blankUrl_returnsValidationErrorResponse() throws Exception {
        CreateUrlRequestDto dto = new CreateUrlRequestDto(" ");

        mvc.perform(post("/url")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"))
                .andExpect(jsonPath("$.details.url").exists());

        verifyNoInteractions(urlService);
    }

    // ------------------- SUCCESS -------------------

    @Test
    @DisplayName("GET /url/{hash} → 302 + Location header when URL exists")
    void redirect_returns302_andLocationHeader() throws Exception {
        when(urlService.getOriginalUrl("abc123"))
                .thenReturn("https://example.com");

        mvc.perform(get("/url/abc123")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    // ------------------- EXCEPTIONS FROM SERVICE -------------------

    @Test
    @DisplayName("GET /url/{hash} → 404 + url_not_found when service throws UrlNotFoundException")
    void redirect_notFound_returns404_errorResponse() throws Exception {
        when(urlService.getOriginalUrl("missing"))
                .thenThrow(new UrlNotFoundException("Invalid hash: missing"));

        mvc.perform(get("/url/missing")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("url_not_found"))
                .andExpect(jsonPath("$.message").value("Invalid hash: missing"));

        verify(urlService).getOriginalUrl("missing");
    }

    @Test
    @DisplayName("GET /url/{hash} → 400 + bad_request when IllegalArgumentException occurs")
    void redirect_illegalArgument_returns400_errorResponse() throws Exception {
        when(urlService.getOriginalUrl("abc123"))
                .thenThrow(new IllegalArgumentException("Bad request"));

        mvc.perform(get("/url/abc123")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("bad_request"))
                .andExpect(jsonPath("$.message").value("Bad request"));

        verify(urlService).getOriginalUrl("abc123");
    }

    @Test
    @DisplayName("GET /url/{hash} → 500 + internal_error on unexpected exception")
    void redirect_runtimeException_returns_errorResponse() throws Exception {
        when(urlService.getOriginalUrl("abc123"))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/url/abc123")
                        .header(USER_ID_HEADER, USER_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("internal_error"))
                .andExpect(jsonPath("$.message").value("Internal server error"));

        verify(urlService).getOriginalUrl("abc123");
    }
}
