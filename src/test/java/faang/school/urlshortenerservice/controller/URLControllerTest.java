package faang.school.urlshortenerservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.config.context.UserHeaderFilter;
import faang.school.urlshortenerservice.dto.UrlRequest;
import faang.school.urlshortenerservice.service.URLService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(URLController.class)
public class URLControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserHeaderFilter userHeaderFilter;

    @MockBean
    private URLService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createHashTest() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl("https://test-tracker.com/");
        mockMvc.perform(MockMvcRequestBuilders.post("/sh.c")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
