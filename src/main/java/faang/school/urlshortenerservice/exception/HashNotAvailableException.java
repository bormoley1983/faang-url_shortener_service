package faang.school.urlshortenerservice.exception;

public class HashNotAvailableException extends RuntimeException {

    public HashNotAvailableException(String message) {
        super(message);
    }

    public HashNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}