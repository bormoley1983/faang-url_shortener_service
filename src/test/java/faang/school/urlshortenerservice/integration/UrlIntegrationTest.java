package faang.school.urlshortenerservice.integration;

import faang.school.urlshortenerservice.AbstractIntegrationTest;
import faang.school.urlshortenerservice.dto.CreateUrlRequestDto;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.service.HashCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UrlIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UrlRepository urlRepository;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @MockBean
    HashCache hashCache; // контролируем hash в тесте

    private static final String USER_ID_HEADER = "x-user-id";

    @Test
    void postUrl_persistsToDb_andSavesToRedis() throws Exception {
        when(hashCache.getHash()).thenReturn("ABC123");

        String longUrl = "https://example.com/some/long/url";
        CreateUrlRequestDto dto = new CreateUrlRequestDto(longUrl);

        mvc.perform(post("/url")
                        .header(USER_ID_HEADER, "1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // DB
        assertThat(urlRepository.findById("ABC123")).isPresent();

        // Redis
        String cached = stringRedisTemplate.opsForValue().get("url:ABC123");
        assertThat(cached).isEqualTo(longUrl);
    }
}