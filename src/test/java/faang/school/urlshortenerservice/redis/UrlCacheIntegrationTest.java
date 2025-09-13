package faang.school.urlshortenerservice.redis;

import faang.school.urlshortenerservice.model.UrlEntity;
import faang.school.urlshortenerservice.repository.UrlRepository;
import faang.school.urlshortenerservice.util.ContainerConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UrlCacheIntegrationTest extends ContainerConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("Редирект записывает значение в Redis и использует кэш")
    void redirectCachesInRedisTest() throws Exception {
        UrlEntity entity = UrlEntity.builder()
                .hash("cache1")
                .url("https://example.com")
                .build();
        urlRepository.save(entity);

        mockMvc.perform(get("/sh.c/cache1")
                        .header("x-user-id", 1))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));

        String cachedUrl = redisTemplate.opsForValue().get("cache1");
        assertThat(cachedUrl).isEqualTo("https://example.com");

        mockMvc.perform(get("/sh.c/cache1")
                        .header("x-user-id", 1))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://example.com"));
    }
}