package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public record UrlRequestDto(
        @NotBlank(message = "Url сannot be empty")
        @URL(message = "Invalid URL format")
        @Pattern(regexp = "^(http|https)://", message = "Only HTTP/HTTPS protocols allowed")
        @Pattern(regexp = "\\.(com|org|net|ru)(/|:|$)", message = "Only .com, .org, .net domains allowed")
        String url
) {
}
