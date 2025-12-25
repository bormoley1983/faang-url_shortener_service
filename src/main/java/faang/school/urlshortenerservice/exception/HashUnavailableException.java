package faang.school.urlshortenerservice.exception;

public class HashUnavailableException extends RuntimeException {
    public HashUnavailableException() {
        super("Unable to generate unique hash.");
    }
}
