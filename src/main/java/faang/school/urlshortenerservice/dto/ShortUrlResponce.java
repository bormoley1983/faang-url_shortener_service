package faang.school.urlshortenerservice.dto;

import java.net.URI;
import java.time.LocalDateTime;

public record ShortUrlResponce(
        URI url,
        LocalDateTime expireTime
) {
}
