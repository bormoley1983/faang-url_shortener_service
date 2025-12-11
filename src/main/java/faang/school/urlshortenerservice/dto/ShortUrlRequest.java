package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public record ShortUrlRequest(
        @NotNull
        @URL
        String url,

        @Future
        LocalDateTime expireTime
) {
}
