package faang.school.urlshortenerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public record UrlRequestDto(
        @NotBlank(message = "Url сannot be empty")
        @URL(message = "Invalid URL format")
        @Pattern(
                regexp = "^(https?://)[\\w.-]+\\.(com|org|net|ru)(:[0-9]+)?(/.*)?$",
                message = "Only HTTP/HTTPS protocols and .com, .org, .net, .ru domains allowed"
        )
        String url
) {
}
