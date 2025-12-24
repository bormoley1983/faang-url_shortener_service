package faang.school.urlshortenerservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.BaseContext;
import faang.school.urlshortenerservice.dto.UrlRequest;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class URLControllerTest extends BaseContext {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlRepository urlRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Интеграционный тест на создание сокращенной ссылки")
    void createHashTest() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl("https://test-tracker.com/");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/short-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("http://localhost:8080/short-url/1"));
    }
}