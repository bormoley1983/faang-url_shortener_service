package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UrlResponseDto(@NotBlank(message = "URL is empty")
                             @URL(message = "Your link should start with \"http:// or https://\"")
                             String url) {
}