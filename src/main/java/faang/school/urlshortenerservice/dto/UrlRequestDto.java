package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UrlRequestDto(
        @NotBlank(message = "URL must not be empty")
        @Pattern(
                regexp = "^(https?://).+",
                message = "URL must start with http:// or https://"
        )
        String url
) {
}
