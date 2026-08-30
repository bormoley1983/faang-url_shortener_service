package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShortUrlRequest(
        @NotBlank(message = "url must not be blank")
        @Size(max = 2048, message = "url must not exceed 2048 characters")
        String url
) {
}
