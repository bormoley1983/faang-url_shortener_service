package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UrlRequestDto(
        @NotBlank(message = "URL cannot be empty")
        @Size(max = 2048, message = "The URL must not exceed 2048 characters")
        String originalUrl
) {
}
