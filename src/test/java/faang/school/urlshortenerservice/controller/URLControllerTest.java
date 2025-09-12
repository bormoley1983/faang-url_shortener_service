package faang.school.urlshortenerservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.urlshortenerservice.BaseContext;
import faang.school.urlshortenerservice.dto.UrlRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createHashTest() throws Exception {
        UrlRequest request = new UrlRequest();
        request.setUrl("https://test-tracker.com/");
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/short-url")
                        .header("x-user-id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("http://localhost:8080/sh.c/1"));
    }
}