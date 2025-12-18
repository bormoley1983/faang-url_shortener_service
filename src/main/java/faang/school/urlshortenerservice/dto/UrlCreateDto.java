package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UrlCreateDto(
        @NotBlank(message = "Url сan`t be empty")
        @URL(message = "Invalid URL format")
        String userUrl
) {
}