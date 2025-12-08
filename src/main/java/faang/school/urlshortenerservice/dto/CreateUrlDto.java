package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateUrlDto(
        @NotBlank(message = "URL cannot be blank")
        @URL(message = "Invalid URL format")
        String url
) {
}