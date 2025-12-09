package faang.school.urlshortenerservice.exception;

public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException() {
    }

    public UrlNotFoundException(String message) {
        super(message);
    }

    public UrlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlNotFoundException(Throwable cause) {
        super(cause);
    }
}
