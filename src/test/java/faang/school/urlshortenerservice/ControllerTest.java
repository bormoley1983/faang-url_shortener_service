package faang.school.urlshortenerservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.dto.UrlRequestDto;
import faang.school.urlshortenerservice.dto.UrlShortDto;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Тест контроллера UrlController")
class ControllerTest {

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

        MvcResult result = mockMvc.perform(post("/short")
                        .header("x-user-id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        UrlShortDto responseDto = objectMapper.readValue(response, UrlShortDto.class);

        assertThat(responseDto.shortUrl()).startsWith("http://localhost:8080/short/");
    }

    @Test
    @DisplayName("Попытка редиректа по несуществующему хэшу")
    void redirectToNonExistentHashTest() throws Exception {
        mockMvc.perform(get("/short/invalid")
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

        mockMvc.perform(get("/short/abc123")
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

        mockMvc.perform(get("/short/test12")
                        .header("x-user-id", 1))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));
    }
}