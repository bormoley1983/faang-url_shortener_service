package faang.school.urlshortenerservice.integration.controller;

import faang.school.urlshortenerservice.integration.base.AbstractIntegrationTest;
import faang.school.urlshortenerservice.config.UrlCacheProperties;
import faang.school.urlshortenerservice.dto.CreateUrlRequestDto;
import faang.school.urlshortenerservice.repository.db.UrlRepository;
import faang.school.urlshortenerservice.service.HashCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
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
class UrlEndToEndIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UrlRepository urlRepository;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UrlCacheProperties urlCacheProperties;
    @MockBean
    HashCache hashCache;

    private static final String USER_ID_HEADER = "x-user-id";

    @Test
    @DisplayName("Trigger controller POST and hash generation, check redis and DB have stored new hash and longUrl")
    void postUrl_persistsToDb_andSavesToRedis() throws Exception {
        when(hashCache.getHash()).thenReturn("ABC123");

        String longUrl = "https://example.com/some/long/url";
        CreateUrlRequestDto dto = new CreateUrlRequestDto(longUrl);

        mvc.perform(post("/url")
                        .header(USER_ID_HEADER, "1")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        assertThat(urlRepository.findById("ABC123")).isPresent();

        String expectedKey = urlCacheProperties.getModule()
                + ":"
                + urlCacheProperties.getVersion()
                + ":"
                + urlCacheProperties.getUrlEntity()
                + ":ABC123";
        String cached = stringRedisTemplate.opsForValue().get(expectedKey);
        assertThat(cached).isEqualTo(longUrl);
    }
}