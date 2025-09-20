package faang.school.urlshortenerservice.data;

import faang.school.urlshortenerservice.dto.UrlViewDto;
import faang.school.urlshortenerservice.integrationTests.controller.UrlControllerTestIT;
import org.springframework.http.ResponseEntity;

/**
 * Тестовые данные для контроллера {@link UrlControllerTestIT}
 *
 * @author Linempy
 * @since 16.09.2025
 */
public class UrlControllerTestData {

    public static final String ORIGIN_URL = "https://domen_example.com/courses/4jnzmndg/32dnjy9d";

    public static String getHashFromResponse(ResponseEntity<UrlViewDto> response) {
        String[] splitShortUrl = response.getBody().shortUrl().split("/");
        return splitShortUrl[splitShortUrl.length - 1];
    }
}