package faang.school.urlshortenerservice.exception;

public class UrlNotFoundException extends RuntimeException{
    public UrlNotFoundException(String hash) {
        super("Ссылка с хэшем '" + hash + "' не найдена");
    }
}