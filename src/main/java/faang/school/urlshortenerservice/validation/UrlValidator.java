package faang.school.urlshortenerservice.validation;

import faang.school.urlshortenerservice.entity.ShortUrl;
import faang.school.urlshortenerservice.exception.UrlExpiredException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class UrlValidator {
    public void validateUrlNotExpired(ShortUrl shortUrl) {
        if(Objects.nonNull(shortUrl.getExpire_time()) && shortUrl.getExpire_time().isBefore(LocalDateTime.now())) {
            throw new UrlExpiredException(shortUrl.getHash(), shortUrl.getExpire_time());
        }

    }
}
