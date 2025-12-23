package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record ShortUrlRequest(
        @NotNull
        @URL
        String url
) {
}
