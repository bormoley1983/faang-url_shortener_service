package faang.school.urlshortenerservice.dto;

public record ErrorResponse(String code, String message, Object details) {
    public ErrorResponse(String code, String message) {
        this(code, message, null);
    }
}
