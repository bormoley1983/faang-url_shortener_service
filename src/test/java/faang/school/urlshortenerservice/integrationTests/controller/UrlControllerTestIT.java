package faang.school.urlshortenerservice.integrationTests.controller;

import faang.school.urlshortenerservice.config.BaseIntegrationTest;
import faang.school.urlshortenerservice.dto.UrlCreateDto;
import faang.school.urlshortenerservice.dto.UrlViewDto;
import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.repository.UrlCacheRepository;
import faang.school.urlshortenerservice.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static faang.school.urlshortenerservice.data.UrlControllerTestData.getHashFromResponse;
import static org.awaitility.Awaitility.await;
import static faang.school.urlshortenerservice.data.UrlControllerTestData.ORIGIN_URL;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Тестирование UrlController")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UrlControllerTestIT extends BaseIntegrationTest {

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UrlCacheRepository cacheRepository;

    @LocalServerPort
    private int port;

    private HttpHeaders headers;
    private String absoluteUrl;

    @BeforeEach
    void setUp() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-user-id", "3");
        headers.set("User-Agent", "TestRestTemplate");

        absoluteUrl = String.format("http://localhost:%s/urls", port);
    }

    @Test
    @DisplayName("POST - должен создать и вернуть короткий URL")
    void shouldCreateShortUrl() {
        UrlCreateDto createDto = new UrlCreateDto(ORIGIN_URL);

        HttpEntity<UrlCreateDto> requestEntity = new HttpEntity<>(createDto, headers);
        ResponseEntity<UrlViewDto> response = testRestTemplate
                .postForEntity(absoluteUrl, requestEntity, UrlViewDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        UrlEntity urlEntity = urlRepository.findByUrlOrThrows(createDto.longUrl());

        await().atMost(3, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    Optional<String> originUrlFromCache = cacheRepository.findOriginUrlByHash(urlEntity.getHash());
                    return originUrlFromCache.isPresent();
                });

        Optional<String> originUrlFromCache = cacheRepository.findOriginUrlByHash(urlEntity.getHash());
        String hashFromResponse = getHashFromResponse(response);

        assertThat(hashFromResponse).isEqualTo(urlEntity.getHash());
        assertThat(createDto.longUrl()).isEqualTo(originUrlFromCache.get());
    }

    @Test
    @DisplayName("GET /{hash} - должен выполнить редирект на оригинальный URL")
    void shouldGetOriginUrl() {
        UrlEntity urlEntity = urlRepository.findByUrlOrThrows(ORIGIN_URL);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = testRestTemplate
                .exchange(String.format(absoluteUrl + "/%s", urlEntity.getHash()), HttpMethod.GET, requestEntity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(response.getHeaders().getLocation())
                .isNotNull()
                .hasToString(ORIGIN_URL);
    }
}