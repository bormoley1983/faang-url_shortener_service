package faang.school.urlshortenerservice.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String hash) {
        super("URL с хешем " + hash + " не найден в базе данных!");
    }
}
