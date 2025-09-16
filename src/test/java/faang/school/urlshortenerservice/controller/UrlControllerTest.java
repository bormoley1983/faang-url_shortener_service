package faang.school.urlshortenerservice.controller;

import faang.school.urlshortenerservice.model.dto.UrlRequestDto;
import faang.school.urlshortenerservice.model.dto.UrlResponseDto;
import faang.school.urlshortenerservice.model.UrlEntity;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Тест контроллера UrlController")
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlRepository urlRepository;

    @Test
    @DisplayName("Создание короткой ссылки")
    void createShortUrlTest() throws Exception {
        UrlRequestDto requestDto = new UrlRequestDto("https://example.com");

        MvcResult result = mockMvc.perform(post("/sh.c")
                        .header("x-user-id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        UrlResponseDto responseDto = objectMapper.readValue(response, UrlResponseDto.class);

        assertThat(responseDto.shortUrl()).startsWith("http://localhost:8080/sh.c/");
    }

    @Test
    @DisplayName("Попытка редиректа по несуществующему хэшу")
    void redirectToNonExistentHashTest() throws Exception {
        mockMvc.perform(get("/sh.c/invalid")
                        .header("x-user-id", 1))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Редирект по существующему хэшу")
    void redirectToLongUrlIntegrationTest() throws Exception {
        UrlEntity entity = UrlEntity.builder()
                .hash("abc123")
                .url("https://example.com")
                .build();
        urlRepository.save(entity);

        mockMvc.perform(get("/sh.c/abc123")
                        .header("x-user-id", 1))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));
    }

    @Test
    @DisplayName("Редирект на длинную ссылку по хэшу")
    void redirectToLongUrlTest() throws Exception {

        UrlEntity entity = UrlEntity.builder()
                .hash("test12")
                .url("https://example.com")
                .build();
        urlRepository.save(entity);

        mockMvc.perform(get("/sh.c/test12")
                        .header("x-user-id", 1))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));
    }
}