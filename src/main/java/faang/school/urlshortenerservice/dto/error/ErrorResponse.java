package faang.school.urlshortenerservice.dto.error;

public record ErrorResponse(
        String error,
        String message
) {}
