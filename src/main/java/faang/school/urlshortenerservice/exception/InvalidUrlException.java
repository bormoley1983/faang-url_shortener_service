package faang.school.urlshortenerservice.exception;

public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String url, String reason) {
        super(String.format("Invalid URL '%s': %s", url, reason));
    }
}
