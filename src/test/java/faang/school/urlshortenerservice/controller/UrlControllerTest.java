package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.config.context.UserContext;
import faang.school.urlshortenerservice.dto.CreateUrlRequestDto;
import faang.school.urlshortenerservice.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlController.class)
@DisplayName("POST /url validation")
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

    @Test
    @DisplayName("Returns 400 Bad Request when URL is not a valid HTTP URL")
    void createUrl_invalidUrl_returns400() throws Exception {
        CreateUrlRequestDto dto = new CreateUrlRequestDto("not-a-url");

        mvc.perform(post("/url")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(urlService);
    }

    @Test
    @DisplayName("Returns 400 Bad Request when URL is blank")
    void createUrl_blankUrl_returns400() throws Exception {
        CreateUrlRequestDto dto = new CreateUrlRequestDto(" ");

        mvc.perform(post("/url")
                        .header(USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(urlService);
    }
}