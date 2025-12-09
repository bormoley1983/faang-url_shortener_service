package faang.school.urlshortenerservice.exception;

public record ErrorResponse(
        int status,
        String message
) {
}
