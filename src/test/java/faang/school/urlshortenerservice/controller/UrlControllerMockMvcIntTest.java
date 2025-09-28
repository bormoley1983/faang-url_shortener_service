package faang.school.urlshortenerservice.controller;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlControllerMockMvcIntTest {

    @Autowired
    protected MockMvc mockMvc;

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:13.6");

    @Container
    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse(
                    "redis/redis-stack:latest"));

    @DynamicPropertySource
    static void setContainerProperties(DynamicPropertyRegistry registry) {
        POSTGRESQL_CONTAINER.start();
        REDIS_CONTAINER.start();

        registry.add("spring.datasource.url",
                POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username",
                POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password",
                POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.data.redis.port",
                () -> REDIS_CONTAINER.getMappedPort(6379));
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${content.url}")
    private String url = "https://www.google.com/search?q=amsterdam+sights&rlz=1C5CHFA_enAM1020AM1022&sxsrf=AJOqlzVpeoKgccah6fWoJknYVkBsUzU26A:1678654067076&source=lnms&tbm=isch&sa=X&ved=2ahUKEwj6qPnaodf9AhWkgf0HHYwjBvwQ_AUoAXoECAEQAw&biw=1440&bih=789&dpr=2imgrc=2F4KvjYofOuZIM";

    @Test
    void testCreateShortUrl() throws Exception {
        mockMvc.perform(
                        post("/url")
                                .header("x-user-id", 999)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"url\":\"" + url + "\"}")
                )
                .andExpect(status().isCreated());
    }

    @Test
    void testRedirect() throws Exception {
        mockMvc.perform(
                get("/100001")
                        .header("x-user-id", 333))
                .andExpect(status().isFound());
    }
}
