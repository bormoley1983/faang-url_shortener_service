package faang.school.urlshortenerservice.exception;

public class UrlExpiredException extends RuntimeException {
    public UrlExpiredException(String hash) {
        super("URL with hash " + hash + " has expired");
    }
}
