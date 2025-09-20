package faang.school.urlshortenerservice.data;

import faang.school.urlshortenerservice.entity.UrlEntity;
import faang.school.urlshortenerservice.unitTests.service.UrlServiceImplTest;

import java.time.LocalDateTime;

import static faang.school.urlshortenerservice.data.UrlControllerTestData.ORIGIN_URL;

/**
 * Тестовые данные для сервиса {@link UrlServiceImplTest}
 *
 * @author Linempy
 * @since 17.09.2025
 */
public class UrlServiceImplTestData {
    public static final String DEFAULT_HASH = "1";

    public static UrlEntity getDefaultUrlEntity() {
        UrlEntity url = new UrlEntity();
        url.setHash(DEFAULT_HASH);
        url.setUrl(ORIGIN_URL);
        url.setCreatedAt(LocalDateTime.now());
        return url;
    }
}