package faang.school.urlshortenerservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.dto.UrlDto;
import faang.school.urlshortenerservice.exception.ValidationException;
import faang.school.urlshortenerservice.exception.handler.GlobalExceptionHandler;
import faang.school.urlshortenerservice.service.UrlService;
import faang.school.urlshortenerservice.validator.PayloadValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(GlobalExceptionHandler.class)
@ContextConfiguration(classes = UrlController.class)
class UrlControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UrlService urlService;
    @MockBean
    private PayloadValidator validator;

    private static final String HASH = "999999";
    private static final String LONG_URL = "https://somesource.ru/super/long/url";
    private static final String INVALID_URL = "https:";
    private static final String SHORT_URL = "https://test.org/" + HASH;

    @Test
    @DisplayName("200 OK - POST /v1/urls/short")
    void positive_shouldCallShorten() throws Exception {
        UrlDto dto = new UrlDto(LONG_URL, null);
        UrlDto expected = new UrlDto(SHORT_URL, HASH);
        when(urlService.getShortUrl(dto)).thenReturn(expected);

        mockMvc.perform(post("/v1/urls/short")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(toJson(expected), true));

        verify(urlService, times(1)).getShortUrl(dto);
    }

    @Test
    @DisplayName("200 OK - GET /v1/urls/redirect/{hash}")
    void positive_shouldCallRedirect() throws Exception {
        when(urlService.getOriginalUrl(HASH)).thenReturn(LONG_URL);

        mockMvc.perform(get("/v1/urls/redirect/{hash}", HASH))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", LONG_URL));

        verify(urlService, times(1)).getOriginalUrl(HASH);
    }

    @Test
    @DisplayName("400 BAD REQUEST - POST /v1/urls/short")
    void negative_whenRequestBodyNotValid_returns400BadRequest() throws Exception {
        UrlDto dto = new UrlDto(INVALID_URL, null);
        doThrow(ValidationException.class).when(validator).validateUrl(dto.url());

        mockMvc.perform(post("/v1/urls/short")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest());

        verify(urlService, never()).getShortUrl(any(UrlDto.class));
    }

    @Test
    @DisplayName("400 BAD REQUEST - GET /v1/urls/redirect/{hash}")
    void negative_whenPathVarIsBlank_returns400BadRequest() throws Exception {
        mockMvc.perform(get("/v1/urls/redirect/{hash}", " "))
                .andExpect(status().isBadRequest());

        verify(urlService, never()).getOriginalUrl(anyString());
    }

    // ----------------------------

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}