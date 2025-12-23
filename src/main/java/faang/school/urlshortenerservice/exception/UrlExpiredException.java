package faang.school.urlshortenerservice.exception;

import java.time.LocalDateTime;

public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String hash, LocalDateTime expireTime) {
        super(String.format("Short URL with hash '%s' expired at %s", hash, expireTime));
    }
}
